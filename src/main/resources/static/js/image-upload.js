document.addEventListener('DOMContentLoaded', function() {
    const coverImageInput = document.getElementById('coverImage');
    const imagePreview = document.getElementById('imagePreview');
    const preview = document.getElementById('preview');
    const clearButton = document.getElementById('clearImage');
    const coverImageBase64 = document.getElementById('coverImageBase64');
    const currentImageDisplay = document.getElementById('currentImageDisplay');

    // ファイル選択時の処理
    coverImageInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                // プレビュー表示
                preview.src = e.target.result;
                imagePreview.style.display = 'block';
                currentImageDisplay.style.display = 'none';
                // hidden inputに画像データを設定
                coverImageBase64.value = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    });

    // クリアボタンの処理
    clearButton.addEventListener('click', function() {
        // ファイル入力をリセット
        coverImageInput.value = '';
        // プレビューをクリア
        imagePreview.style.display = 'none';
        preview.src = '';
        // hidden inputをクリア
        coverImageBase64.value = '';
        // 現在の画像表示を元に戻す（デフォルト画像を表示）
        currentImageDisplay.style.display = 'block';
        // デフォルト画像のパスを取得して設定
        const defaultImage = currentImageDisplay.querySelector('img');
        if (defaultImage && !coverImageBase64.value) {
            defaultImage.src = '/images/default-book-cover.svg';
        }
    });
});



async function scanImage() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
        const video = document.createElement('video');
        const scannerDiv = document.createElement('div');
        scannerDiv.id = 'scanner';
        scannerDiv.style.position = 'fixed';
        scannerDiv.style.top = '0';
        scannerDiv.style.left = '0';
        scannerDiv.style.width = '100%';
        scannerDiv.style.height = '100%';
        scannerDiv.style.backgroundColor = 'rgba(0,0,0,0.8)';
        scannerDiv.style.zIndex = '9999';
        
        const videoContainer = document.createElement('div');
        videoContainer.style.position = 'relative';
        videoContainer.style.width = '80%';
        videoContainer.style.maxWidth = '640px';
        videoContainer.style.margin = '50px auto';
        
        const closeButton = document.createElement('button');
        closeButton.textContent = '閉じる';
        closeButton.className = 'btn btn-secondary';
        closeButton.style.position = 'absolute';
        closeButton.style.top = '10px';
        closeButton.style.right = '10px';
        
        video.srcObject = stream;
        video.style.width = '100%';
        
        videoContainer.appendChild(video);
        videoContainer.appendChild(closeButton);
        scannerDiv.appendChild(videoContainer);
        document.body.appendChild(scannerDiv);
        
        await video.play();
        
        // QRコードスキャナーの初期化
        const barcodeDetector = new BarcodeDetector({
            formats: ['isbn_13', 'isbn_10', 'upc_a', 'ean_13']
        });
        
        // スキャンループ
        async function scanLoop() {
            try {
                const barcodes = await barcodeDetector.detect(video);
                if (barcodes.length > 0) {
                    const barcode = barcodes[0].rawValue;
                    // バーコードが検出されたら、GoogleBooks APIを呼び出す
                    const response = await fetch(`/api/books/scan?isbn=${barcode}`);
                    if (response.ok) {
                        const bookData = await response.json();
                        // フォームに値を設定
                        document.getElementById('title').value = bookData.title || '';
                        document.getElementById('author').value = bookData.author || '';
                        if (bookData.coverImage) {
                            // プレビュー画像の更新
                            const preview = document.getElementById('preview');
                            preview.src = bookData.coverImage;
                            document.getElementById('imagePreview').style.display = 'block';
                            document.getElementById('currentImageDisplay').style.display = 'none';
                            document.getElementById('coverImageBase64').value = bookData.coverImage;
                        }
                        // スキャナーを閉じる
                        closeScan();
                    }
                }
                if (scannerDiv.parentNode) { // スキャナーがまだ表示されている場合
                    requestAnimationFrame(scanLoop);
                }
            } catch (error) {
                console.error('Scanning error:', error);
            }
        }
        
        scanLoop();
        
        // 閉じるボタンのイベントリスナー
        closeButton.addEventListener('click', closeScan);
        
        function closeScan() {
            stream.getTracks().forEach(track => track.stop());
            if (scannerDiv.parentNode) {
                scannerDiv.parentNode.removeChild(scannerDiv);
            }
        }
        
    } catch (error) {
        console.error('Failed to start camera:', error);
        alert('カメラの起動に失敗しました');
    }
}