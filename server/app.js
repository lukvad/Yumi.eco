http = require('http');
const interval = 1500;





for (i=1;i<21;i++){
		(function (i){
			setInterval(function (){
				var link = {
					host: 'www.'+i+'.lukvad.usermd.net',
					port: 80,
					path: '/945229/'+i+'/,,,/0/0/0',
					method: 'GET'
				};
					var req = http.request(link, function(res) {
				});
		
				req.on('error', function(e) {
					console.log('problem with request: ' + e.message);
				});
				// write data to request body

				req.end();
		},2000);
	})(i);
}

