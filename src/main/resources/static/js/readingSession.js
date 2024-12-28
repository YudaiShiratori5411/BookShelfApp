let editModal;

document.addEventListener('DOMContentLoaded', function() {
    editModal = new bootstrap.Modal(document.getElementById('editModal'));
});

// セッション編集関数
function editSession(sessionId) {
    // URLから本のIDを取得
    const pathParts = window.location.pathname.split('/');
    const bookId = pathParts[2]; // /books/5/progress の "5" を取得

    // 現在のセッションデータを取得
    fetch(`/books/${bookId}/progress/sessions/${sessionId}`, {
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(session => {
        // フォームに値を設定
        document.getElementById('editStartTime').value = formatDateTime(session.startTime);
        document.getElementById('editEndTime').value = formatDateTime(session.endTime);
        document.getElementById('editPagesRead').value = session.pagesRead;
        
        // フォームのアクションURLを設定
        const form = document.getElementById('editForm');
        form.action = `/books/${bookId}/progress/sessions/${sessionId}`;
        
        // モーダルを表示
        editModal.show();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('セッションデータの取得に失敗しました。');
    });
}

// 日時フォーマット関数
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '';
    return dateTimeStr.slice(0, 16); // すでにISO形式の文字列として受け取る想定
}

// フォームサブミット前の検証
document.getElementById('editForm')?.addEventListener('submit', function(e) {
    const startTime = new Date(document.getElementById('editStartTime').value);
    const endTime = new Date(document.getElementById('editEndTime').value);
    
    if (endTime <= startTime) {
        e.preventDefault();
        alert('終了時間は開始時間より後である必要があります。');
        return false;
    }
});

// DELETE操作の確認
function confirmDelete(sessionId) {
    return confirm('このセッションを削除してもよろしいですか？');
}

// モーダルを閉じる関数
function closeEditModal() {
    editModal.hide();
}