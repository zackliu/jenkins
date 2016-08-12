var request = require('request');
var rf = require('fs');
var https = require('https');
var data = rf.readFileSync("createjobs.groovy", "utf-8");
//console.log(data);

var times = 3;
var finalurl = "";
var config = require('./systemConfig/config.json');
if(typeof URL !== 'undefined' && URL) finalurl = URL;
else finalurl = 'http://' + encodeURIComponent(config['administrator'][0]['username']) + ':' + encodeURIComponent("#Bugsfor$") + '@' + config.url.substr(7) + 'scriptText';


function doPost(){
    console.log(finalurl);
    request.post(
        {
            url : finalurl,
            form:
            {
                //user : 'admin:admin',
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