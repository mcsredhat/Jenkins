from flask import Flask, jsonify
import os
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Get configuration from environment
app.config['ENV'] = os.getenv('ENVIRONMENT', 'development')
PORT = int(os.getenv('APP_PORT', 5000))

@app.route('/')
def hello():
    """Main endpoint"""
    return jsonify({
        'message': 'Hello from Flask Python App!',
        'version': '1.0.0',
        'environment': app.config['ENV'],
        'status': 'running'
    })

@app.route('/health')
def health():
    """Health check endpoint for Docker and Jenkins"""
    return jsonify({
        'status': 'healthy',
        'service': 'flask-python-app',
        'version': '1.0.0'
    }), 200

@app.route('/info')
def info():
    """Application info endpoint"""
    return jsonify({
        'app_name': 'Flask Python Application',
        'version': '1.0.0',
        'environment': app.config['ENV'],
        'port': PORT,
        'flask_version': '3.1.0'
    })

@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors"""
    return jsonify({
        'error': 'Not Found',
        'message': 'The requested resource was not found',
        'status_code': 404
    }), 404

@app.errorhandler(500)
def internal_error(error):
    """Handle 500 errors"""
    logger.error(f"Internal server error: {error}")
    return jsonify({
        'error': 'Internal Server Error',
        'message': 'An internal server error occurred',
        'status_code': 500
    }), 500

if __name__ == '__main__':
    logger.info(f"Starting Flask application on port {PORT}")
    logger.info(f"Environment: {app.config['ENV']}")
    
    # Run the application
    app.run(
        host='0.0.0.0',
        port=PORT,
        debug=(app.config['ENV'] == 'development')
    )
