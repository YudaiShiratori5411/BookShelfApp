console.log('BookFormScanner.js loading...');

document.addEventListener('DOMContentLoaded', function() {
    // フォーム要素の参照を取得
    const titleInput = document.getElementById('title');
    const authorInput = document.getElementById('author');
    const totalPagesInput = document.getElementById('totalPages');
    const coverImageBase64 = document.getElementById('coverImageBase64');
    const preview = document.getElementById('preview');
    const imagePreview = document.getElementById('imagePreview');
    const currentImageDisplay = document.getElementById('currentImageDisplay');

    let modalElement = null;
    let codeReader = null;

    // ISBNから本の情報を取得する関数
    async function fetchBookInfo(isbn) {
        try {
            console.log('Fetching book info for ISBN:', isbn);

            // まずGoogle Books APIを試す
            console.log('Trying Google Books API');
            const googleResponse = await fetch(
                `/api/books/google?isbn=${isbn}`,
                {
                    headers: {
                        'Accept': 'application/json'
                    }
                }
            );
            const googleData = await googleResponse.json();

            if (googleData.items && googleData.items.length > 0) {
                console.log('Book found in Google Books:', googleData.items[0]);
                const book = googleData.items[0].volumeInfo;

                // フォームに情報を設定
                titleInput.value = book.title || '';
                authorInput.value = book.authors ? book.authors.join(', ') : '';
                totalPagesInput.value = book.pageCount || '不明';

                // 表紙画像の処理
                if (book.imageLinks) {
                    try {
                        let imageUrl = book.imageLinks.thumbnail || book.imageLinks.smallThumbnail;
                        imageUrl = imageUrl.replace('http:', 'https:').replace('zoom=1', 'zoom=2');
                        
                        console.log('Trying to fetch image from:', imageUrl);
                        
                        const imageResponse = await fetch(`/api/proxy-image?url=${encodeURIComponent(imageUrl)}`);
                        if (!imageResponse.ok) {
                            throw new Error(`Image fetch failed: ${imageResponse.status}`);
                        }
                        
                        const blob = await imageResponse.blob();
                        console.log('Image blob received:', blob);
                        
                        console.log('Converting blob to base64...');
                        
                        const reader = new FileReader();
                        
                        reader.onload = function(e) {
                            console.log('Image conversion completed, result length:', e.target.result.length);
                            console.log('Setting image preview...');
                            
                            if (!preview || !imagePreview || !currentImageDisplay || !coverImageBase64) {
                                console.error('Required DOM elements not found:', {
                                    preview: !!preview,
                                    imagePreview: !!imagePreview,
                                    currentImageDisplay: !!currentImageDisplay,
                                    coverImageBase64: !!coverImageBase64
                                });
                                return;
                            }
                            
                            setTimeout(() => {
                                preview.src = e.target.result;
                                imagePreview.style.display = 'block';
                                currentImageDisplay.style.display = 'none';
                                coverImageBase64.value = e.target.result;
                                
                                console.log('Preview update completed:', {
                                    previewSrc: preview.src.substring(0, 50) + '...',
                                    imagePreviewDisplay: imagePreview.style.display,
                                    currentImageDisplay: currentImageDisplay.style.display,
                                    base64Value: coverImageBase64.value.substring(0, 50) + '...'
                                });
                            }, 0);
                        };
                        
                        reader.onerror = function(error) {
                            console.error('Error reading blob:', error);
                        };
                        
                        reader.readAsDataURL(blob);
                    } catch (error) {
                        console.error('Cover image error:', error);
                        alert('表紙画像の読み込みに失敗しました');
                    }
                }
                return;
            }

            // Google Booksで見つからない場合は楽天Books APIを試す
            console.log('Book not found in Google Books, trying Rakuten API');
            const rakutenResponse = await fetch(`/api/books/rakuten?isbn=${isbn}`);
            const rakutenData = await rakutenResponse.json();
            console.log('Rakuten API response:', rakutenData);

            if (rakutenData && rakutenData.success) {
                console.log('Book found in Rakuten:', rakutenData);
                
                // フォームに情報を設定
                titleInput.value = rakutenData.title || '';
                authorInput.value = rakutenData.author || '';
                totalPagesInput.value = rakutenData.pageCount || '不明';

                // 表紙画像の処理
                if (rakutenData.coverUrl) {
                    try {
                        const imageResponse = await fetch(
                            `/api/proxy-image?url=${encodeURIComponent(rakutenData.coverUrl)}`
                        );
                        const blob = await imageResponse.blob();
                        const reader = new FileReader();
                        
                        reader.onload = function(e) {
                            preview.src = e.target.result;
                            imagePreview.style.display = 'block';
                            currentImageDisplay.style.display = 'none';
                            coverImageBase64.value = e.target.result;
                        };
                        
                        reader.readAsDataURL(blob);
                    } catch (error) {
                        console.error('Cover image error:', error);
                        alert('表紙画像の読み込みに失敗しました');
                    }
                }
                return;
            }

            // どちらのAPIでも見つからなかった場合
            console.log('Book not found in any API');
            alert('本の情報が見つかりませんでした');

        } catch (err) {
            console.error('Error details:', {
                message: err.message,
                stack: err.stack,
                status: err.status
            });
            alert('本の情報の取得に失敗しました');
        }
    }

    // HTML文字列からエレメントを作成する補助関数
    function createElementFromHTML(htmlString) {
        const div = document.createElement('div');
        div.innerHTML = htmlString.trim();
        return div.firstChild;
    }

    // バーコードスキャン開始
    async function startScanning() {
        console.log('Starting scan process');

        try {
            console.log('Requesting camera access');
            const stream = await navigator.mediaDevices.getUserMedia({
                video: {
                    facingMode: 'environment',
                    width: { ideal: 1280 },
                    height: { ideal: 720 }
                }
            });
            console.log('Camera access granted');

            const video = document.createElement('video');
            video.srcObject = stream;
            video.style.width = '100%';
            video.style.height = '100%';
            video.style.objectFit = 'cover';
            await video.play();
            console.log('Video element playing');

            modalElement = createElementFromHTML(`
                <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5);">
                    <div class="modal-dialog modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title">バーコードスキャン</h5>
                                <button type="button" class="btn-close"></button>
                            </div>
                            <div class="modal-body">
                                <div id="scanner-container" style="width: 100%; height: 300px; position: relative; overflow: hidden;">
                                    <div style="position: absolute; top: 0; left: 0; right: 0; bottom: 0; border: 2px solid red; pointer-events: none;"></div>
                                </div>
                                <div class="text-center mt-2">
                                    <small class="text-muted">バーコードをスキャン範囲内に合わせてください</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `);

            document.body.appendChild(modalElement);
            const scannerContainer = modalElement.querySelector('#scanner-container');
            scannerContainer.appendChild(video);
            
            // ZXingの初期化
            codeReader = new ZXing.BrowserMultiFormatReader();
            
            // スキャン開始
            try {
                const result = await codeReader.decodeFromVideoDevice(null, video, (result, err) => {
                    if (result) {
                        console.log('Barcode detected:', result.text);
                        stopScanning();
                        fetchBookInfo(result.text);
                    }
                    if (err && !(err instanceof ZXing.NotFoundException)) {
                        console.error('Scan error:', err);
                    }
                });
            } catch (error) {
                console.error('Scanning error:', error);
                alert('スキャンに失敗しました');
                stopScanning();
            }

            // モーダルを閉じるボタンのイベントリスナー
            modalElement.querySelector('.btn-close').addEventListener('click', stopScanning);

        } catch (err) {
            console.error('Camera error:', err);
            alert('カメラの起動に失敗しました');
        }
    }

    // スキャン停止処理
    function stopScanning() {
        if (codeReader) {
            codeReader.reset();
            codeReader = null;
        }
        if (modalElement) {
            modalElement.remove();
            modalElement = null;
        }
    }

    // スキャンボタンのイベントリスナーを設定
    const scanButton = document.getElementById('scanButton');
    if (scanButton) {
        console.log('Scan button found');
        scanButton.addEventListener('click', function(e) {
            e.preventDefault();
            startScanning();
        });
    } else {
        console.error('Scan button not found in DOM');
    }
});

