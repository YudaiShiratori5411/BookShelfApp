document.addEventListener('DOMContentLoaded', function() {
    const coverImageInput = document.getElementById('coverImage');
    const imagePreview = document.getElementById('imagePreview');
    const preview = document.getElementById('preview');
    const clearButton = document.getElementById('clearImage');
    const coverImageBase64 = document.getElementById('coverImageBase64');
    const currentImageDisplay = document.getElementById('currentImageDisplay');

    // 既存の画像がある場合の処理
    if (coverImageBase64.value) {
        currentImageDisplay.style.display = 'block';
    }

    // ファイル選択時の処理
    coverImageInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                // プレビュー表示
                preview.src = e.target.result;
                imagePreview.style.display = 'block';
                // 現在の画像を非表示
                if (currentImageDisplay) {
                    currentImageDisplay.style.display = 'none';
                }
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
        // 現在の画像表示を制御
        if (currentImageDisplay) {
            currentImageDisplay.style.display = 'none';
        }
    });
});