'use strict';

var app = angular.module('app', ['meterGauge','PostService','ngWebsocket','ngResource']);

app.controller('MyCtrl', function($scope,$interval,SendPostReq, $timeout,$websocket,$http,$resource){

$scope.speedometer = [];
var vehicles = [];
var kit = [];
var ws = [];
$scope.scenarioStatus = {};
var response;
$scope.api_getSetup = {};
$scope.date = new Date();
var vehiclesInSetup = [];
var myEl = angular.element( document.querySelector( '#terminal' ) );



/* REST API URLS */

var portAddress = 'http://localhost:8080/anki/rest'
var setupURL = portAddress + '/setup';
var scenarioURL = portAddress+ '/setup'


/* REST API URLS ends here*/



/* Scenario Starts here*/

$scope.scenarioArray = [ "anti-collision", "collision","Scenario A" ];





$scope.checkBoxClicked = function($checkbox,$index)
{
    console.log($checkbox,$index);
    console.log($scope.scenarioArray[$index]);

    var action = $checkbox ? 'start' : 'interupt';

    for(var i=0; i<$scope.api_getSetup.length;i++)
    {
        $scope.sendReq(scenarioURL+'/'+$scope.api_getSetup[i].uuid+'/scenario/'+$scope.scenarioArray[$index]+'/'+action);
    }

    $scope.updateTerminalStatus($scope.scenarioArray[$index], $checkbox);
}

/* Scenario ends here */


/* Terminal starts here*/


$scope.updateTerminalStatus = function($scenarioName,$status)
{
    $scope.newDate();
    
    var statusnew = $status ? "Preparing to start" : "Stopping";

    myEl.append('>> ['+$scope.date+'] '+ statusnew +' '+ $scenarioName +'... '+'<br>');

    console.log("update terminal");

}

$scope.newDate = function () {
      $scope.date = new Date();
}


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

console.log($scope.api_getSetup);


$scope.refreshSetupAPI = function()
{

    var setupData = $resource('http://demo1910725.mockable.io/data');
            
            setupData.query(function(data)
            {

                var x  = angular.toJson(data);
                $scope.api_getSetup = angular.fromJson(x);
                $scope.createSpeedoMeter(); // creating speedometer again

 
            });


   /* for(var i=0;i<$scope.api_getSetup.length;i++)
    {

         var scenarioData = $resource(scenarioURL+'/'+$scope.api_getSetup[i].uuid+'/scenario');
            scenarioData.query(function(data){

                var x  = angular.toJson(data);
                $scope.scenarioArray = angular.fromJson(x);
         
 
            });

    } */


    var scenarioData = $resource('http://demo1910725.mockable.io/');
           
            scenarioData.query(function(data)
            {

                var x  = angular.toJson(data);
                $scope.scenarioArray = angular.fromJson(x);
         
 
            });

};

$scope.refreshSetupAPI(); //initially fetching the data from the rest API



/* REST SERVICE FUNCTIONS ends here*/



/* WEBSOCKET STARTS HERE */

$scope.webSocketConnection = function()
{


    for(var i=0; i<$scope.api_getSetup.length;i++) //connecting all websocket ports from the rest API
    {

        ws[$scope.api_getSetup[i].uuid].$on('$open', function () {

            for(var j=0;j<$scope.api_getSetup.length;j++)
            {

                ws[$scope.api_getSetup[j].uuid].$emit('webgui',''+$scope.api_getSetup[j].uuid+' '+$scope.api_getSetup[j].websocket); // it sends the event on connection

             }


        })
        .$on('$message',function (message) { // it listents for incoming 'messages'

            console.log(message);

            if(message.command === "enable-listener")
            {

                  $timeout($scope.speedometer[message.vehicleId].needleVal = message.payload.speed,2); 
            }

            
    

        });
    }

};


/* WEBSOCKET ENDS HERE*/




/* CARS CONTROLLER STARTS HERE */


$scope.createSpeedoMeter = function()
{
    console.log($scope.api_getSetup);


    kit.length = 0;
    ws.length = 0;
    $scope.speedometer.length = 0;
    vehicles.length = 0;
    $scope.allVehicles = null;



for(var i=0; i<$scope.api_getSetup.length;i++)
{

    kit = $scope.api_getSetup[i];
    ws[kit.uuid] = $websocket.$new(kit.websocket);
    for(var j=0 ; j< kit.vehicles.length; j++)
    {
        console.log(kit.vehicles[j]);
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
                gaugeUnits: "cms",
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
$scope.webSocketConnection(); // establishing websocket connections


$scope.sendWebSocketMessage = function (setupID,vehicleID,messageType,value)
{


    if(messageType === 'connection')
    {
        $scope.refreshSetupAPI(); // fetching new data from API about all the available cars and setups
        $scope.webSocketConnection(); // establishing websocket connections

    
        var val = value ? "disconnect" : "connect";
        
        var json_conn = [{
                                "command" : ""+val,
                                "vehicleId": ""+vehicleID
                            }];

        var val = value ? "disable-listener" : "enable-listener";

        var json_listener = [{
                                "command" : ""+val,
                                "vehicleId": ""+vehicleID
                            }];


        ws[setupID].$emit('webgui',json_conn);
        ws[setupID].$emit('webgui',json_listener);
    }

    else if(messageType === 'changeSpeed')
    {
        
        var websocket_setupid = $scope.getSetupID(vehicleID.substring(1));
        var new_json = {
                            "command" : 'set-speed',
                            "vehicleId" : vehicleID.substring(1),
                            "payload" : {'speed' : value}

                        };

        //$timeout($scope.speedometer[vehicleID].needleVal = value, 10);       
        ws[websocket_setupid].$emit('webgui',new_json);

    }

    else if(messageType === 'changeLane')
    {
        console.log("changelane was called");

        var websocket_setupid = $scope.getSetupID(vehicleID.substring(2));
        var new_json = {
                            "command" : 'set-offset',
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


$scope.getSetupID = function(vehicleid)
{
    
    return vehiclesInSetup[vehicleid];

}








/* CARS CONTROLLER ENDS HERE */





	
	
	
});