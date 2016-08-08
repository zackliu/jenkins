var request = require('request');
var rf = require('fs');
var https = require('https');
var data = rf.readFileSync("jobMonitor.groovy", "utf-8");
//console.log(data);

var times = 10;

function doPost(){
    var finalurl = 'http://localhost:8080/scriptText';
    console.log(finalurl);
    request.post(
        {
            url : finalurl,
            form:
            {
                script : data.toString()
            }
        },
        function(error, response, body)
        {
            if(response != null &&  response.statusCode == 200)
            {
                console.log(body);
            }
                
            else if (response == null || response.statusCode == 503)
            {
                if(response != null && times <= 0) console.log(response.statusCode);
                else
                {
                    console.log("Wait........");
                    times = times - 1;
                    setTimeout(doPost, 5000);
                }
            }
            else
            {
                console.log(response.statusCode);
            }
                
        }
        
    );
}

doPost();