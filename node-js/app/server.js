const http = require('http');
const os = require('os');

// Track requests for demo purposes
let requestCount = 0;

const server = http.createServer((req, res) => {
  requestCount++;
  
  // Simple routing
  if (req.url === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'healthy', uptime: process.uptime() }));
    return;
  }
  
  if (req.url === '/crash') {
    console.error('Manual crash triggered');
    process.exit(1);
  }
  
  // Regular response
  res.writeHead(200, { 'Content-Type': 'text/html' });
  res.end(`<html>
    <body>
      <h1>Container Demo</h1>
      <p>Hostname: ${os.hostname()}</p>
      <p>Total requests: ${requestCount}</p>
      <p>Uptime: ${process.uptime().toFixed(2)} seconds</p>
    </body>
  </html>`);
  
  // Log each request
  console.log(`Request received: ${req.url} (total: ${requestCount})`);
});

server.listen(3000, () => {
  console.log('Server started on port 3000');
});

// Log memory usage every 30 seconds
setInterval(() => {
  const memoryUsage = process.memoryUsage();
  console.log(`Memory usage: ${Math.round(memoryUsage.rss / 1024 / 1024)} MB`);
}, 30000);
