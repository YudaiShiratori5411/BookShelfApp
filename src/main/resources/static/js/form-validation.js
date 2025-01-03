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









// DOMが読み込まれた後に実行
//document.addEventListener('DOMContentLoaded', function() {
//    // フォーム要素の取得
//    const form = document.querySelector('form');
//    
//    if (form) {
//        // submitイベントのリスナーを追加
//        form.addEventListener('submit', function(event) {
//            // デフォルトの送信をキャンセル
//            event.preventDefault();
//            
//            // 入力値の取得
//            const title = document.getElementById('title');
//            const author = document.getElementById('author');
//            const totalPages = document.getElementById('totalPages');
//            const category = document.querySelector('#category');
//            
//            // エラーメッセージを格納する配列
//            let errors = [];
//            
//            // バリデーションチェック
//            if (!title.value.trim()) {
//                errors.push('タイトルが入力されていません');
//            }
//            
//            if (!author.value.trim()) {
//                errors.push('著者が入力されていません');
//            }
//            
//            if (!totalPages.value || parseInt(totalPages.value) <= 0) {
//                errors.push('総ページ数が入力されていません');
//            }
//            
//            if (!category.value || category.value === 'カテゴリーを選択') {
//                errors.push('カテゴリーを選んでください');
//            }
//            
//            // エラーがある場合
//            if (errors.length > 0) {
//                // エラーメッセージをまとめて表示
//                alert(errors.join('\n'));
//                return false;
//            }
//            
//            // エラーがない場合はフォームを送信
//            form.removeAttribute('novalidate');
//            form.submit();
//        });
//        
//        // 各入力フィールドのデフォルトバリデーションメッセージを無効化
//        const inputs = form.querySelectorAll('input, select');
//        inputs.forEach(input => {
//            input.addEventListener('invalid', (e) => {
//                e.preventDefault();
//            });
//        });
//    }
//});


