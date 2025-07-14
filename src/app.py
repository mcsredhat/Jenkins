from flask import Flask
import os
app = Flask(__name__)
env = os.environ.get('ENVIRONMENT', 'development')
@app.route('/')
def hello():
    return f"Hello from Docker Lesson 4! Running in {env} environment."

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
