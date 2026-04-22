<?php
$mediaDir = 'media'; // Ordnername anpassen

$imageExt = ['jpg', 'jpeg', 'png', 'gif', 'webp'];
$videoExt = ['mp4', 'webm', 'ogg'];

$files = scandir($mediaDir);
$media = [];

foreach ($files as $file) {
    $path = $mediaDir . '/' . $file;
    if (!is_file($path)) {
        continue;
    }

    $ext = strtolower(pathinfo($path, PATHINFO_EXTENSION));

    if (in_array($ext, $imageExt)) {
        $media[] = [
            'type' => 'image',
            'src'  => $path
        ];
    } elseif (in_array($ext, $videoExt)) {
        $media[] = [
            'type' => 'video',
            'src'  => $path
        ];
    }
}

if (count($media) === 0) {
    echo "Keine Medien im Ordner '$mediaDir' gefunden.";
    exit;
}
?>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>Slideshow Bilder & Videos</title>
    <style>
        #slideshow {
            width: 100%;
            height: 100%;
            margin: 0 auto;
            background: #000;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        #slideshow img,
        #slideshow video {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
        }
    </style>
</head>
<body>

<div id="slideshow"></div>

<script>
const media = <?php echo json_encode($media, JSON_UNESCAPED_SLASHES); ?>;
let index = 0;
const container = document.getElementById('slideshow');

function showMedia(i) {
    const item = media[i];
    container.innerHTML = '';

    if (item.type === 'image') {
        const img = document.createElement('img');
        img.src = item.src;
        img.alt = 'Slideshow Bild';
        container.appendChild(img);
    } else if (item.type === 'video') {
        const video = document.createElement('video');
        video.src = item.src;
        video.autoplay = true;
        video.loop = true;      // falls du nur 15s willst, loop ggf. entfernen
        video.muted = true;     // für Auto-Play in vielen Browsern nötig
        video.controls = true;  // falls du Bedienelemente willst
        container.appendChild(video);
    }
}

function nextMedia() {
    index = (index + 1) % media.length;
    showMedia(index);
}

// erstes Element anzeigen
showMedia(index);

// alle 15 Sekunden wechseln
setInterval(nextMedia, 2000);
</script>

</body>
</html>
