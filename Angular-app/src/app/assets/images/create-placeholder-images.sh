#!/bin/bash
# create-placeholder-images.sh
# Script to generate placeholder images for the Angular portfolio

# Create the images directory if it doesn't exist
mkdir -p src/assets/images

# Function to create a placeholder image using ImageMagick (if available)
# Otherwise, create simple HTML placeholder files
create_placeholder() {
    local filename=$1
    local width=$2
    local height=$3
    local text=$4
    local color=$5
    
    if command -v convert &> /dev/null; then
        # Using ImageMagick to create actual images
        convert -size ${width}x${height} xc:${color} \
                -gravity center \
                -pointsize 24 \
                -fill white \
                -annotate +0+0 "${text}" \
                "src/assets/images/${filename}"
        echo "Created: src/assets/images/${filename}"
    else
        # Create SVG placeholder instead
        cat > "src/assets/images/${filename%.jpg}.svg" << EOF
<svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
  <rect width="100%" height="100%" fill="${color}"/>
  <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" 
        font-family="Arial, sans-serif" font-size="24" fill="white">
    ${text}
  </text>
</svg>
EOF
        echo "Created: src/assets/images/${filename%.jpg}.svg"
    fi
}

echo "Creating placeholder images for Angular Portfolio..."

# Hero section background
create_placeholder "hero-bg.jpg" 1920 1080 "Hero Background" "#667eea"

# About section background
create_placeholder "about-bg.jpg" 1920 1080 "About Background" "#764ba2"

# Profile photo placeholder
create_placeholder "profile-photo.jpg" 400 400 "Profile Photo" "#4a90e2"

# Project screenshots
create_placeholder "project1.jpg" 800 600 "E-Commerce Platform" "#2ecc71"
create_placeholder "project2.jpg" 800 600 "Task Management App" "#e74c3c"
create_placeholder "project3.jpg" 800 600 "Weather Dashboard" "#f39c12"
create_placeholder "project4.jpg" 800 600 "Blog API Service" "#9b59b6"
create_placeholder "project5.jpg" 800 600 "Real-time Chat App" "#1abc9c"
create_placeholder "project6.jpg" 800 600 "Portfolio Website" "#34495e"

# Skills section background
create_placeholder "skills-bg.jpg" 1920 1080 "Skills Background" "#667eea"

# Contact section background
create_placeholder "contact-bg.jpg" 1920 1080 "Contact Background" "#764ba2"

# Logo placeholder
create_placeholder "logo.png" 200 60 "LOGO" "#667eea"

echo ""
echo "Placeholder images created successfully!"
echo ""
echo "Note: If you have ImageMagick installed, actual images were created."
echo "Otherwise, SVG placeholders were created that you can replace later."
echo ""
echo "To install ImageMagick:"
echo "  Ubuntu/Debian: sudo apt-get install imagemagick"
echo "  macOS: brew install imagemagick"
echo "  Windows: Download from https://imagemagick.org/script/download.php"