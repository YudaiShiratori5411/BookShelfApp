document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('coverImage');
    const preview = document.getElementById('preview');
    const previewContainer = document.getElementById('imagePreview');
    const clearButton = document.getElementById('clearImage');
    const base64Input = document.getElementById('coverImageBase64');

    // 最大ファイルサイズ（2MB）
    const MAX_FILE_SIZE = 2 * 1024 * 1024;

    fileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (!file) return;

        // ファイルサイズチェック
        if (file.size > MAX_FILE_SIZE) {
            alert('ファイルサイズは2MB以下にしてください。');
            fileInput.value = '';
            return;
        }

        // 画像ファイル形式チェック
        if (!file.type.match('image.*')) {
            alert('画像ファイルを選択してください。');
            fileInput.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            const img = new Image();
            img.onload = function() {
                // 画像のリサイズが必要な場合（例：幅が1000pxを超える場合）
                if (img.width > 1000) {
                    const canvas = document.createElement('canvas');
                    const ctx = canvas.getContext('2d');
                    const maxWidth = 1000;
                    const scale = maxWidth / img.width;
                    
                    canvas.width = maxWidth;
                    canvas.height = img.height * scale;
                    
                    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                    const resizedBase64 = canvas.toDataURL(file.type);
                    
                    preview.src = resizedBase64;
                    base64Input.value = resizedBase64;
                } else {
                    preview.src = e.target.result;
                    base64Input.value = e.target.result;
                }
                previewContainer.style.display = 'block';
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    });

    clearButton.addEventListener('click', function() {
        fileInput.value = '';
        base64Input.value = '';
        previewContainer.style.display = 'none';
        preview.src = '';
    });

    // 既存の画像がある場合のプレビュー表示
    if (base64Input.value) {
        preview.src = base64Input.value;
        previewContainer.style.display = 'block';
    }
});