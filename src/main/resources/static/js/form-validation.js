document.addEventListener('DOMContentLoaded', function() {
    'use strict'
    
    const form = document.querySelector('form');
    
    // 必須項目のバリデーションメッセージを非表示にする
    const currentPageInput = document.getElementById('currentPage');
    if (currentPageInput) {
        currentPageInput.removeAttribute('required');
    }
    
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const title = document.getElementById('title');
            const author = document.getElementById('author');
            const totalPages = document.getElementById('totalPages');
            const category = document.getElementById('category');
            
            let errorMessages = [];
            
            // 必須項目のチェック
            if (!title.value.trim()) {
                errorMessages.push('タイトルが入力されていません');
            }
            
            if (!author.value.trim()) {
                errorMessages.push('著者が入力されていません');
            }
            
            if (!totalPages.value || totalPages.value <= 0) {
                errorMessages.push('総ページ数が入力されていません');
            }
            
            if (!category.value || category.value === 'カテゴリーを選択') {
                errorMessages.push('カテゴリーを選んでください');
            }
            
            // エラーメッセージがある場合
            if (errorMessages.length > 0) {
                // 全てのエラーメッセージをまとめて表示
                alert(errorMessages.join('\n'));
                return false;
            }
            
            // バリデーションが成功した場合のみフォームを送信
            form.submit();
        });
        
        // HTMLのデフォルトバリデーションメッセージを無効化
        const inputs = form.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.addEventListener('invalid', (e) => {
                e.preventDefault();
            });
        });
    }
});



