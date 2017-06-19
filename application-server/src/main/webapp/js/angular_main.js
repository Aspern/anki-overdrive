'use strict';

var app = angular.module('app', ['meterGauge','PostService','ngWebsocket','ngResource','filters.stringUtils']);

app.controller('MyCtrl', function($scope,$interval,SendPostReq, $timeout,$websocket,$http,$resource){

    $scope.speedometer = [];
    var vehicles = [];
    var kit = [];
    var ws = [];
    var interval_vehicle = [];
    $scope.scenarioStatus = {};
    var response;
    $scope.api_getSetup = {};
    $scope.date = new Date();
    var vehiclesInSetup = [];
    var myEl = angular.element( document.querySelector( '#terminal' ) );
    $scope.scenarioArray = ["collision","anti-collision"];
    $scope.battery_level = [];
    $scope.disable = {};
    $scope.parameter_div = false;



    /* REST API URLS */

//var portAddress = 'http://localhost:8080/rest';
//var setupURL = portAddress + '/setup';
    var scenarioURL = '/rest/setup';


    /* REST API URLS ends here*/

    $scope.setParameterDiv = function(val){

        $scope.parameter_div = val;
    };

    $scope.getParameterDiv = function(){

        return $scope.parameter_div;
    };

    $scope.cancelAntiCollision = function()
    {
        $scope.scenarioStatus['anti-collision'] = false;

    };


    /* Scenario Starts here*/


    $scope.checkBoxClicked = function($checkbox,$index)
    {
        var action = $checkbox ? 'start' : 'interrupt';

        if(($index == 0 && $checkbox == false) || ($index == 1 && $checkbox == false) || ($index == 0 && $checkbox == true) ) // if the scenario is collision and anti-collision is false
        {
            console.log("stop anti collision");
            for(var i=0; i<$scope.api_getSetup.length;i++) //for all kits
            {
                $scope.sendReq(scenarioURL+'/'+$scope.api_getSetup[i].ean+'/scenario/'+$scope.scenarioArray[$index]+'/'+action);
            }
            $scope.updateTerminalStatus($scope.scenarioArray[$index], $checkbox);

        }

        if($index == 1 && $checkbox==false)
            $scope.setParameterDiv(false);
        else if($index == 1 && $checkbox==true)
            $scope.setParameterDiv(true);

    };

    $scope.startAntiCollisionScenario = function()
    {

        var speed_gs = angular.element('#GroundShock').val();
        var speed_sk =  angular.element('#Skull').val();
        var lane_no = angular.element('#lane_anticollision').val();
        var break_s = angular.element('#range_break_anticollision').val();
        var accel = angular.element('#range_acceleration_anticollision').val();
        var distance =  angular.element('#range_distance_anticollision').val();

        for(var i=0; i<$scope.api_getSetup.length;i++) //for all kits
        {
            $scope.sendReq(scenarioURL+'/'+$scope.api_getSetup[i].ean+'/scenario/'+$scope.scenarioArray[1]+'/'+'start?speed_GS='+speed_gs+"&"+'speed_SK='+speed_sk+"&"+'lane='+lane_no+"&"+'break='+break_s+"&"+'accel='+accel+"&"+'distance='+distance);
            console.log(scenarioURL+'/'+$scope.api_getSetup[i].ean+'/scenario/'+$scope.scenarioArray[1]+'/'+'start?speed_GS='+speed_gs+"&"+'speed_SK='+speed_sk+"&"+'lane='+lane_no+"&"+'break='+break_s+"&"+'accel='+accel+"&"+'distance='+distance);
        }
        $scope.updateTerminalStatus("Anti collision", true);


    };

    /* Scenario ends here */


    /* Terminal starts here*/
    $scope.date = new Date();

    myEl.append('>> ['+$scope.date+'] Establishing connection with the system... '+'<br>');

    $scope.updateTerminalStatus = function($scenarioName,$status)
    {
        $scope.newDate();

        var statusnew = $status ? "Preparing to start" : "Stopping";

        myEl.append('>> ['+$scope.date+'] '+ statusnew +' '+ $scenarioName +'... '+'<br>');

    };

    $scope.newDate = function () {
        $scope.date = new Date();
    };


    /* Terminal ends here*/

    /* POST service */


    $scope.sendReq = function (url,data) {

        response = SendPostReq.sendPost(url,data);

        response.error(function (response) {
            console.log('Error');
            $timeout(function(){

                console.log("not connected");


            }, 2000);
        });
    };

    /* POST service ends here */


    /* REST SERVICE FUNCITONS */

    $scope.refreshSetupAPI = function()
    {

        //'http://demo1910725.mockable.io/data'
        var setupData = $resource('/rest/setup');

        setupData.query(function(data)
        {
            var x  = angular.toJson(data);
            $scope.api_getSetup = angular.fromJson(x);
            $scope.createSpeedoMeter(); // creating speedometer again


        });


        for(var i=0;i<$scope.api_getSetup.length;i++)
        {

            var scenarioData = $resource('/rest/setup'+'/'+$scope.api_getSetup[i].ean+'/scenario');
            scenarioData.query(function(data){

                var x  = angular.toJson(data);
                $scope.scenarioArray = angular.fromJson(x);


            });

        }


    };

    $scope.refreshSetupAPI(); // fetching REST data onLoad

    $scope.sendConnectionRequest = function(url,value)
    {

        var val = value ? "disconnect" : "connect";

        $scope.sendReq(url+val);
        setTimeout(function(){

            $scope.refreshSetupAPI();

        },800);


    };



    /* REST SERVICE FUNCTIONS ends here*/

//send messages to websocket
    $scope.sendWebSocketMessage = function (setupID,vehicleID,messageType,value)
    {
        if(messageType == 'connection')
        {
            var val = value ? "disable-listener" : "enable-listener";

            var json_listener = {
                "command" : ""+val,
                "vehicleId": ""+vehicleID
            };

            ws[setupID].$emit('webgui',json_listener);

            if(value)
            {
                clearInterval(interval_vehicle[vehicleID]); // clearing battery level request if car is disconnected
                interval_vehicle.length = 0; // clearing the array
            }
            else if(!value)
            {

                if(interval_vehicle[vehicleID]== null)
                {
                    //this is called for the 2nd time after the websocket connection is already established and user connects the car again
                    interval_vehicle[vehicleID]= setInterval(function(){

                        var json_battery_listener = {
                            "command" : "query-battery-level",
                            "vehicleId": ""+vehicleID
                        };

                        ws[setupID].$emit('webgui',json_battery_listener);

                    },6000);
                }

            }

        }

        else if(messageType == 'changeSpeed')
        {

            var websocket_setupid = $scope.getSetupID(vehicleID.substring(1));
            var new_json = {
                "command" : 'set-speed',
                "vehicleId" : vehicleID.substring(1),
                "payload" : {'speed' : value}

            };

            if(value >= 0 || value <= 170)
                $timeout($scope.speedometer[vehicleID.substring(1)].needleVal = value, 10);
            console.log($scope.speedometer[vehicleID.substring(1)]);

            ws[websocket_setupid].$emit('webgui',new_json);

        }

        else if(messageType == 'changeLane')
        {

            var websocket_setupid = $scope.getSetupID(vehicleID.substring(2));
            var new_json = {
                "command" : 'change-lane',
                "vehicleId" : vehicleID.substring(2),
                "payload" : {'offset' : value}

            };
            ws[websocket_setupid].$emit('webgui',new_json);

        }

        else if(messageType == 'applyBrake')
        {
            var websocket_setupid = $scope.getSetupID(vehicleID.substring(1));
            var new_json = {
                "command" : 'brake',
                "vehicleId" : vehicleID.substring(1),
                "payload" : {}

            };
            ws[websocket_setupid].$emit('webgui',new_json);

        }


    };



    /* WEBSOCKET STARTS HERE */

    $scope.webSocketConnection = function(setupID,vehicleID,messageType,value)
    {

        ws[setupID].$on('$open', function () {

            if(value)
            {
                $scope.sendWebSocketMessage(setupID,vehicleID,messageType,false);
                // sending enable-listener when there is a websocket connection.


                //sending battery level request once the connection is made
                interval_vehicle[vehicleID]= setInterval(function(){

                    var json_battery_listener = {
                        "command" : "query-battery-level",
                        "vehicleId": ""+vehicleID
                    };

                    ws[setupID].$emit('webgui',json_battery_listener);

                },6000);
            }



        })
            .$on('$message',function (message) { // it listents for incoming 'messages'


                if(message.command === "enable-listener")
                {
                    $scope.speedometer[message.vehicleId].needleVal = message.payload.speed;
                    //console.log(message.payload);

                }
                else if(message.command == "query-battery-level")
                {
                    //change battery level here
                    $scope.updateBatteryLevel(message.vehicleId,message.payload.batteryLevel);
                }


            });


    };


    /* WEBSOCKET ENDS HERE*/


    /* CARS CONTROLLER STARTS HERE */


    $scope.createSpeedoMeter = function()
    {
        kit.length = 0;
        ws.length = 0;
        $scope.speedometer.length = 0;
        vehicles.length = 0;
        $scope.allVehicles = null;

        for(var i=0; i<$scope.api_getSetup.length;i++)
        {
            kit = $scope.api_getSetup[i];
            ws[kit.uuid] = $websocket.$new(kit.websocket, 'echo-protocol');
            $scope.webSocketConnection(kit.uuid);

            for(var j=0 ; j< kit.vehicles.length; j++)
            {
                $scope.webSocketConnection(kit.uuid,kit.vehicles[j].uuid,'connection',kit.vehicles[j].connected);

                if(kit.vehicles[j].connected)
                {
                    vehiclesInSetup[kit.vehicles[j].uuid] = kit.uuid;
                    vehicles.push(kit.vehicles[j]);
                    $scope.speedometer[kit.vehicles[j].uuid] = { // creating an array of speedometer with unique car id's
                        gaugeRadius: 150,
                        minVal: 0,
                        maxVal: 1000,
                        needleVal: Math.round(100),
                        tickSpaceMinVal: 10,
                        tickSpaceMajVal: 100,
                        divID: "gaugeBox",
                        gaugeUnits: "mm/s",
                        tickColMaj:'#000066',
                        tickColMin:'#656D78',
                        outerEdgeCol:'#000066',
                        pivotCol:'#434A54',
                        innerCol:'#E6E9ED',
                        unitsLabelCol:'#656D78',
                        tickLabelCol:'#656D78',
                        needleCol: '#000066',
                        defaultFonts:''
                    };

                    if(kit.vehicles[j].name == "Skull") //if the car is red change color scheme of the speedometer
                    {
                        $scope.speedometer[kit.vehicles[j].uuid].needleCol = '#b20000';
                        $scope.speedometer[kit.vehicles[j].uuid].outerEdgeCol = '#b20000';
                        $scope.speedometer[kit.vehicles[j].uuid].tickColMaj= '#b20000';
                    }


                }
            }

        }

        $scope.allVehicles = vehicles;


    };

    $scope.createSpeedoMeter(); // creating speedometer on runtime


    $scope.getSetupID = function(vehicleid)
    {

        return vehiclesInSetup[vehicleid];

    };

    $scope.updateSpeedRange = function(elementID,val)
    {
        var elemid = '#range'+elementID.substring(1);
        var range = angular.element( document.querySelector( elemid ) );
        range.val(val);

    };

    $scope.updateBatteryLevel = function(elementID,val)
    {
        var elemid = '#bat'+elementID;
        var battery = angular.element( document.querySelector( elemid ) );
        battery.val(val);


        $scope.battery_level[elementID] = Math.round(val*100)+"%";



        console.log("updatebattery function"+$scope.battery_level[elementID]);
        $scope.$apply();


    };

    /* CARS CONTROLLER ENDS HERE */


});