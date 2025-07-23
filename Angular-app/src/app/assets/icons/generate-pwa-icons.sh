#!/bin/bash
# generate-pwa-icons.sh
# Script to generate PWA icons for the Angular portfolio

# Create the icons directory if it doesn't exist
mkdir -p src/assets/icons

# Function to create PWA icons
create_pwa_icon() {
    local size=$1
    local filename="icon-${size}x${size}.png"
    
    if command -v convert &> /dev/null; then
        # Using ImageMagick to create actual PNG icons
        convert -size ${size}x${size} xc:none \
                -fill "#667eea" \
                -draw "roundrectangle 0,0 ${size},${size} 15,15" \
                -gravity center \
                -pointsize $((size/8)) \
                -fill white \
                -annotate +0+0 "P" \
                "src/assets/icons/${filename}"
        echo "Created: src/assets/icons/${filename}"
    else
        # Create SVG version
        cat > "src/assets/icons/icon-${size}x${size}.svg" << EOF
<svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
  <rect width="${size}" height="${size}" rx="15" ry="15" fill="#667eea"/>
  <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" 
        font-family="Arial, sans-serif" font-size="$((size/3))" font-weight="bold" fill="white">
    P
  </text>
</svg>
EOF
        echo "Created: src/assets/icons/icon-${size}x${size}.svg"
    fi
}

echo "Generating PWA icons for Angular Portfolio..."

# Generate all required PWA icon sizes
sizes=(72 96 128 144 152 192 384 512)

for size in "${sizes[@]}"; do
    create_pwa_icon $size
done

# Create favicon.ico (16x16 and 32x32 combined)
if command -v convert &> /dev/null; then
    convert -size 16x16 xc:none \
            -fill "#667eea" \
            -draw "roundrectangle 0,0 16,16 2,2" \
            -gravity center \
            -pointsize 8 \
            -fill white \
            -annotate +0+0 "P" \
            favicon-16.png
    
    convert -size 32x32 xc:none \
            -fill "#667eea" \
            -draw "roundrectangle 0,0 32,32 4,4" \
            -gravity center \
            -pointsize 16 \
            -fill white \
            -annotate +0+0 "P" \
            favicon-32.png
    
    convert favicon-16.png favicon-32.png src/assets/icons/favicon.ico
    rm favicon-16.png favicon-32.png
    echo "Created: src/assets/icons/favicon.ico"
else
    # Create SVG favicon
    cat > "src/assets/icons/favicon.svg" << EOF
<svg width="32" height="32" xmlns="http://www.w3.org/2000/svg">
  <rect width="32" height="32" rx="4" ry="4" fill="#667eea"/>
  <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" 
        font-family="Arial, sans-serif" font-size="16" font-weight="bold" fill="white">
    P
  </text>
</svg>
EOF
    echo "Created: src/assets/icons/favicon.svg"
fi

# Create Apple touch icon
if command -v convert &> /dev/null; then
    convert -size 180x180 xc:none \
            -fill "#667eea" \
            -draw "roundrectangle 0,0 180,180 25,25" \
            -gravity center \
            -pointsize 60 \
            -fill white \
            -annotate +0+0 "P" \
            "src/assets/icons/apple-touch-icon.png"
    echo "Created: src/assets/icons/apple-touch-icon.png"
else
    cat > "src/assets/icons/apple-touch-icon.svg" << EOF
<svg width="180" height="180" xmlns="http://www.w3.org/2000/svg">
  <rect width="180" height="180" rx="25" ry="25" fill="#667eea"/>
  <text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" 
        font-family="Arial, sans-serif" font-size="60" font-weight="bold" fill="white">
    P
  </text>
</svg>
EOF
    echo "Created: src/assets/icons/apple-touch-icon.svg"
fi

echo ""
echo "PWA icons generated successfully!"
echo ""
echo "Icons created for sizes: ${sizes[*]}"
echo "Additional files: favicon, apple-touch-icon"
echo ""
echo "Note: Replace 'P' with your actual logo or initials."