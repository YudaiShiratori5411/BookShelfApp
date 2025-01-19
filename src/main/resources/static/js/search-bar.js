document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const searchResultModalElement = document.getElementById('searchResultModal');
    const searchResults = document.getElementById('searchResults');
    const noResults = document.getElementById('noResults');
    
        // 要素が存在するか確認
    if (!searchInput || !searchButton) {
        console.log('Search elements not found. This might be the login page.');
        return;
    }
    
    // モーダルの初期化
    const searchResultModal = new bootstrap.Modal(searchResultModalElement);

    // 検索実行関数
    async function performSearch(searchTerm) {
        if (!searchTerm || !searchTerm.trim()) return;

        try {
            searchResults.innerHTML = '';
            noResults.style.display = 'none';

            const response = await fetch(`/api/search?query=${encodeURIComponent(searchTerm.trim())}`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                displayError('検索中にエラーが発生しました');
                return;
            }

            const results = await response.json();
            displayResults(results);
        } catch (error) {
            console.error('Search error:', error);
            displayError('検索中にエラーが発生しました。もう一度お試しください。');
        }
    }

    // 検索結果表示関数
    function displayResults(results) {
        searchResults.innerHTML = '';
        
        if (!results || results.length === 0) {
            displayNoResults();
            return;
        }
    
        // 検索結果を格納するグリッドコンテナを作成
        const gridContainer = document.createElement('div');
        gridContainer.className = 'row g-4 no-margin-top';
    
        results.forEach(book => {
            const card = createBookCard(book);
            gridContainer.appendChild(card);
        });
    
        searchResults.appendChild(gridContainer);
        noResults.style.display = 'none';
        searchResults.style.display = 'block';
        searchResultModal.show();
    }

    // エラー表示関数
    function displayError(message) {
        searchResults.style.display = 'none';
        noResults.textContent = message;
        noResults.style.display = 'block';
        searchResultModal.show();
    }

    // 検索結果なし表示関数
    function displayNoResults() {
        searchResults.style.display = 'none';
        noResults.textContent = '検索結果が見つかりませんでした';
        noResults.style.display = 'block';
        searchResultModal.show();
    }
    
    // 本のカード作成関数
    function createBookCard(book) {
        const div = document.createElement('div');
        div.className = 'col-md-6 col-lg-4'; // カードを3列グリッドで表示
        
        const progress = book.totalPages ? 
            Math.min(100, (book.currentPage / book.totalPages) * 100) : 0;
        
        div.innerHTML = `
            <div class="card h-100">
                <div class="card-body">
                    <h4 class="card-title">${escapeHtml(book.title)}</h4>
                    <div class="book-info">
                        <p class="card-text mb-1">
                            <small class="text-muted">著者: ${escapeHtml(book.author || '不明')}</small>
                        </p>
                        <p class="card-text mb-1">
                            <small class="text-muted">カテゴリー: ${escapeHtml(book.category || '未分類')}</small>
                        </p>
                        <div class="progress mt-2" style="height: 5px;">
                            <div class="progress-bar" 
                                 role="progressbar" 
                                 style="width: ${progress}%;" 
                                 aria-valuenow="${progress}" 
                                 aria-valuemin="0" 
                                 aria-valuemax="100">
                            </div>
                        </div>
                        <p class="card-text mt-1">
                            <small class="text-muted">進捗: ${book.currentPage || 0}/${book.totalPages || 0} ページ (${progress.toFixed(1)}%)</small>
                        </p>
                    </div>
                    <a href="/books/${book.id}" class="btn btn-sm btn-primary mt-2">詳細を見る</a>
                </div>
            </div>
        `;
        
        return div;
    }

    // HTMLエスケープ関数
    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // 検索ボタンクリックのイベントリスナー
    searchButton.addEventListener('click', function() {
        const searchTerm = searchInput.value;
        if (searchTerm && searchTerm.trim().length >= 2) {
            performSearch(searchTerm);
        }
    });

    // Enterキー押下時の処理
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            const searchTerm = this.value;
            if (searchTerm && searchTerm.trim().length >= 2) {
                performSearch(searchTerm);
            }
        }
    });
});

