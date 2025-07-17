<?php
// Database configuration
$host = 'mysql-db';
$db   = 'webappdb';
$user = 'webuser';
$pass = 'webpass';

try {
    // Create PDO connection
    $conn = new PDO("mysql:host=$host;dbname=$db", $user, $pass);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Create table if it doesn't exist
    $conn->exec("CREATE TABLE IF NOT EXISTS messages (
        id INT AUTO_INCREMENT PRIMARY KEY,
        message TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");
    
    // Handle POST request to add message
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && !empty($_POST['message'])) {
        $stmt = $conn->prepare("INSERT INTO messages (message) VALUES (:message)");
        $stmt->bindParam(':message', $_POST['message']);
        $stmt->execute();
        $success_message = "Message added successfully!";
    }
    
    // Fetch all messages
    $stmt = $conn->query("SELECT * FROM messages ORDER BY created_at DESC");
    $messages = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
} catch(PDOException $e) {
    $error_message = "Database connection failed: " . $e->getMessage();
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PHP Apache Web Application</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .status-info {
            background: #f8f9fa;
            padding: 20px;
            border-bottom: 1px solid #e9ecef;
        }
        
        .status-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
        }
        
        .status-item {
            background: white;
            padding: 15px;
            border-radius: 8px;
            border-left: 4px solid #667eea;
        }
        
        .status-item strong {
            color: #333;
            display: block;
            margin-bottom: 5px;
        }
        
        .content {
            padding: 30px;
        }
        
        .form-section {
            margin-bottom: 40px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #333;
        }
        
        textarea {
            width: 100%;
            min-height: 120px;
            padding: 15px;
            border: 2px solid #e9ecef;
            border-radius: 8px;
            font-size: 16px;
            resize: vertical;
            transition: border-color 0.3s ease;
        }
        
        textarea:focus {
            outline: none;
            border-color: #667eea;
        }
        
        .btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px 30px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s ease;
        }
        
        .btn:hover {
            transform: translateY(-2px);
        }
        
        .alert {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        
        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        
        .messages-section h2 {
            color: #333;
            margin-bottom: 20px;
            font-size: 1.8em;
        }
        
        .message {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 15px;
            transition: transform 0.2s ease;
        }
        
        .message:hover {
            transform: translateX(5px);
            border-color: #667eea;
        }
        
        .message-meta {
            color: #6c757d;
            font-size: 0.9em;
            margin-bottom: 10px;
            font-weight: 600;
        }
        
        .message-content {
            color: #333;
            line-height: 1.6;
        }
        
        .no-messages {
            text-align: center;
            color: #6c757d;
            font-style: italic;
            padding: 40px;
        }
        
        @media (max-width: 768px) {
            .container {
                margin: 10px;
            }
            
            .header h1 {
                font-size: 2em;
            }
            
            .content {
                padding: 20px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🚀 PHP Apache Web Application</h1>
            <p>Modern Web Application with Database Integration</p>
        </div>
        
        <div class="status-info">
            <div class="status-grid">
                <div class="status-item">
                    <strong>Application Status</strong>
                    <span>🟢 Running</span>
                </div>
                <div class="status-item">
                    <strong>PHP Version</strong>
                    <span><?php echo PHP_VERSION; ?></span>
                </div>
                <div class="status-item">
                    <strong>Server Time</strong>
                    <span><?php echo date('Y-m-d H:i:s'); ?></span>
                </div>
                <div class="status-item">
                    <strong>Database</strong>
                    <span><?php echo isset($error_message) ? '🔴 Disconnected' : '🟢 Connected'; ?></span>
                </div>
            </div>
        </div>
        
        <div class="content">
            <?php if (isset($success_message)): ?>
                <div class="alert alert-success">
                    ✅ <?php echo htmlspecialchars($success_message); ?>
                </div>
            <?php endif; ?>
            
            <?php if (isset($error_message)): ?>
                <div class="alert alert-error">
                    ❌ <?php echo htmlspecialchars($error_message); ?>
                </div>
            <?php else: ?>
                <div class="form-section">
                    <form method="POST">
                        <div class="form-group">
                            <label for="message">💬 Add a New Message</label>
                            <textarea 
                                name="message" 
                                id="message" 
                                placeholder="Share your thoughts, ideas, or just say hello..." 
                                required
                            ></textarea>
                        </div>
                        <button type="submit" class="btn">Send Message</button>
                    </form>
                </div>
                
                <div class="messages-section">
                    <h2>📝 Recent Messages</h2>
                    <?php if (empty($messages)): ?>
                        <div class="no-messages">
                            <p>No messages yet. Be the first to share something!</p>
                        </div>
                    <?php else: ?>
                        <?php foreach ($messages as $msg): ?>
                            <div class="message">
                                <div class="message-meta">
                                    📅 <?php echo htmlspecialchars($msg['created_at']); ?>
                                </div>
                                <div class="message-content">
                                    <?php echo nl2br(htmlspecialchars($msg['message'])); ?>
                                </div>
                            </div>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </div>
            <?php endif; ?>
        </div>
    </div>
</body>
</html>