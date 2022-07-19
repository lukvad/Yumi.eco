const http = require('http');
const run = require('./run');

const port = process.env.PORT || 3000;
const server = http.createServer(run);


server.listen(port);

