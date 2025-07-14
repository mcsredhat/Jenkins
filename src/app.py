from flask import Flask, jsonify
import os
import sys
from datetime import datetime

app = Flask(__name__)
env = os.environ.get('ENVIRONMENT', 'development')
port = int(os.environ.get('PORT', 5000))

@app.route('/')
def hello():
    return f"Hello from Docker Lesson 4! Running in {env} environment."

@app.route('/health')
def health_check():
    """Health check endpoint for monitoring and CI/CD"""
    return jsonify({
        'status': 'healthy',
        'environment': env,
        'timestamp': datetime.utcnow().isoformat(),
        'version': '1.0.0'
    }), 200

@app.route('/info')
def info():
    """Application information endpoint"""
    return jsonify({
        'app_name': 'Flask Docker App',
        'environment': env,
        'port': port,
        'python_version': sys.version,
        'timestamp': datetime.utcnow().isoformat()
    })

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Not found'}), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({'error': 'Internal server error'}), 500

if __name__ == "__main__":
    print(f"Starting Flask application in {env} environment on port {port}")
    app.run(host='0.0.0.0', port=port, debug=(env == 'development'))
