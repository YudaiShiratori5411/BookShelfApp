let isDragging = false;
let draggedItem = null;

document.addEventListener('DOMContentLoaded', function() {
    console.log('Script loaded');

    // スクロール位置の復元
    const savedPosition = sessionStorage.getItem('scrollPosition');
    const lastShelfId = sessionStorage.getItem('lastShelfId');
    
    if (savedPosition) {
        setTimeout(() => {
            window.scrollTo(0, parseInt(savedPosition));
            
            if (lastShelfId) {
                const lastShelf = document.querySelector(`.books-container[data-shelf-id="${lastShelfId}"]`);
                if (lastShelf) {
                    lastShelf.classList.add('highlight');
                    setTimeout(() => {
                        lastShelf.classList.remove('highlight');
                    }, 1000);
                }
            }
        }, 100);

        sessionStorage.removeItem('scrollPosition');
        sessionStorage.removeItem('lastShelfId');
    }

    // Bootstrap Dropdownの初期化
    const dropdowns = document.querySelectorAll('[data-bs-toggle="dropdown"]');
    dropdowns.forEach(dropdown => {
        new bootstrap.Dropdown(dropdown);
    });

    console.log('Found dropdowns:', dropdowns.length);

    // 順番入れ替えボタンのイベントリスナー
    document.querySelectorAll('.sort-books').forEach(button => {
        console.log('Sort button found:', button);
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('Sort button clicked');
            
            const bookshelf = this.closest('.bookshelf');
            const container = bookshelf.querySelector('.books-container');
            toggleSortMode(container, this);

            // ドロップダウンメニューを閉じる
            const dropdownMenu = this.closest('.dropdown-menu');
            if (dropdownMenu) {
                const dropdown = bootstrap.Dropdown.getInstance(dropdownMenu.previousElementSibling);
                if (dropdown) {
                    dropdown.hide();
                }
            }
        });
    });

    // 仕切り追加ボタンのイベントリスナー
    document.querySelectorAll('.add-divider').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            const shelfId = this.getAttribute('data-shelf-id');
            const container = document.querySelector(`.books-container[data-shelf-id="${shelfId}"]`);
            addNewDivider(container);

            // ドロップダウンメニューを閉じる
            const dropdownMenu = this.closest('.dropdown-menu');
            const dropdownToggle = document.querySelector(`[aria-controls="${dropdownMenu.id}"]`);
            const dropdown = bootstrap.Dropdown.getInstance(dropdownToggle);
            if (dropdown) {
                dropdown.hide();
            }
        });
    });

    // 仕切り削除モードのイベントリスナー
    document.querySelectorAll('.delete-dividers-mode').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const shelfId = this.getAttribute('data-shelf-id');
            const container = document.querySelector(`.books-container[data-shelf-id="${shelfId}"]`);
            toggleDeleteDividersMode(container, this);

            // ドロップダウンメニューを閉じる
            const dropdownMenu = this.closest('.dropdown-menu');
            if (dropdownMenu) {
                const dropdown = bootstrap.Dropdown.getInstance(dropdownMenu.previousElementSibling);
                if (dropdown) {
                    dropdown.hide();
                }
            }
        });
    });

    // 削除ボタンのイベントリスナー
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('delete-divider-btn')) {
            const dividerElement = e.target.closest('.shelf-divider');
            const dividerId = dividerElement.getAttribute('data-divider-id');
            
            if (confirm('この仕切りを削除してもよろしいですか？')) {
                fetch(`/api/dividers/${dividerId}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                        'Cache-Control': 'no-cache'
                    }
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to delete divider');
                    }
                    // 画面から要素を削除
                    dividerElement.remove();
                })
                .catch(error => {
                    console.error('Error deleting divider:', error);
                    alert('仕切りの削除に失敗しました');
                });
            }
        }
    });
    
    // 新しい段追加ボタンのイベントリスナー
    let currentShelfId = null;
    const addShelfModal = new bootstrap.Modal(document.getElementById('addShelfModal'));
    
    document.querySelectorAll('.add-shelf').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // クリックされた段のIDを保存
            currentShelfId = this.getAttribute('data-shelf-id');
            
            // モーダルを表示
            addShelfModal.show();
            
            // ドロップダウンメニューを閉じる
            const dropdownMenu = this.closest('.dropdown-menu');
            const dropdownToggle = document.querySelector(`[aria-controls="${dropdownMenu.id}"]`);
            const dropdown = bootstrap.Dropdown.getInstance(dropdownToggle);
            if (dropdown) {
                dropdown.hide();
            }
        });
    });
    
    // 段の削除機能
    document.querySelectorAll('.delete-shelf').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const shelfId = this.getAttribute('data-shelf-id');
            const shelfName = this.closest('.bookshelf').querySelector('.shelf-name').textContent.trim();
            
            if (confirm(`"${shelfName}"を削除してもよろしいですか？\n※本が登録されている段は削除できません`)) {
                fetch(`/api/shelves/${shelfId}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(data => {
                            throw new Error(data.error || '段の削除に失敗しました');
                        });
                    }
                    window.location.reload();
                })
                .catch(error => {
                    alert(error.message);
                });
            }
            
            // ドロップダウンメニューを閉じる
            const dropdownMenu = this.closest('.dropdown-menu');
            if (dropdownMenu) {
                const dropdown = bootstrap.Dropdown.getInstance(dropdownMenu.previousElementSibling);
                if (dropdown) {
                    dropdown.hide();
                }
            }
        });
    });

    // 保存ボタンのイベントリスナー
    document.getElementById('saveShelf').addEventListener('click', function() {
        const nameInput = document.getElementById('shelfName');
        const name = nameInput.value.trim();
        
        if (!name) {
            alert('段の名前を入力してください。');
            return;
        }

        fetch('/api/shelves', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: name,
                referenceShelfId: currentShelfId  // 現在の段の後に追加するための位置情報
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('段の追加に失敗しました');
            }
            return response.json();
        })
        .then(data => {
            addShelfModal.hide();
            nameInput.value = '';  // フォームをクリア
            window.location.reload();  // 成功したらページをリロード
        })
        .catch(error => {
            alert('段の追加に失敗しました。もう一度お試しください。');
            console.error('Error:', error);
        });
    });

    // モーダルが閉じられたときにフォームをリセット
    document.getElementById('addShelfModal').addEventListener('hidden.bs.modal', function () {
        document.getElementById('shelfName').value = '';
        currentShelfId = null;
    });
});

function toggleSortMode(container, button) {
    const allContainers = document.querySelectorAll('.books-container');
    const isInSortMode = container.classList.contains('sorting-mode');

    try {
        if (!isInSortMode) {
            // ソートモード開始
            allContainers.forEach(cont => {
                cont.classList.add('sorting-mode');
                // 本のリンクを無効化
                const bookLinks = cont.querySelectorAll('.book-link');
                bookLinks.forEach(link => {
                    // href属性を一時的に保存して削除
                    link.dataset.originalHref = link.getAttribute('href');
                    link.removeAttribute('href');
                    // クリックイベントを無効化
                    link.style.pointerEvents = 'none';
                    // クリックイベントをキャンセル
                    link.addEventListener('click', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        return false;
                    }, true);
                });
                enableDragAndDrop(cont);
            });
            if (button) {
                button.textContent = '順番入れ替えを完了';
                button.classList.add('active');
            }
        } else {
            // ソートモード終了処理
            const savePromises = [];
            
            // 現在のスクロール位置を保存
            const scrollPosition = window.pageYOffset || document.documentElement.scrollTop;
            sessionStorage.setItem('scrollPosition', scrollPosition);
            
            // 最後に操作した本棚のIDを保存
            const activeShelfId = container.getAttribute('data-shelf-id');
            if (activeShelfId) {
                sessionStorage.setItem('lastShelfId', activeShelfId);
            }

            // 各コンテナの順序を保存
            allContainers.forEach(cont => {
                savePromises.push(saveNewOrder(cont));
                cont.classList.remove('sorting-mode');
                // 本のリンクを再有効化
                const bookLinks = cont.querySelectorAll('.book-link');
                bookLinks.forEach(link => {
                    // 保存していたhref属性を復元
                    if (link.dataset.originalHref) {
                        link.setAttribute('href', link.dataset.originalHref);
                        delete link.dataset.originalHref;
                    }
                    // クリックイベントを再有効化
                    link.style.pointerEvents = 'auto';
                });
                disableDragAndDrop(cont);
            });

            if (button) {
                button.textContent = '順番入れ替え';
                button.classList.remove('active');
            }

            // すべての保存処理が完了してからリロード
            Promise.all(savePromises)
                .then(results => {
                    const allSuccess = results.every(result => result === true);
                    if (!allSuccess) {
                        throw new Error('Some changes failed to save');
                    }
                    window.location.reload();
                })
                .catch(error => {
                    console.error('Error in save process:', error);
                    alert('並び順の保存に失敗しました。もう一度お試しください。');
                    window.location.reload();
                });
        }
    } catch (error) {
        console.error('Error in toggleSortMode:', error);
        alert('エラーが発生しました。ページをリロードします。');
        window.location.reload();
    }
}

// 仕切り削除モードの切り替え関数
function toggleDeleteDividersMode(container, button) {
    const dividers = container.querySelectorAll('.shelf-divider');
    const isInDeleteMode = !dividers[0]?.classList.contains('delete-mode');
    
    if (isInDeleteMode) {
        button.textContent = '仕切り削除を完了';
        dividers.forEach(divider => {
            divider.classList.add('delete-mode');
            const deleteBtn = divider.querySelector('.delete-divider-btn');
            if (deleteBtn) deleteBtn.style.display = 'block';
        });
    } else {
        button.textContent = '仕切りを削除する';
        dividers.forEach(divider => {
            divider.classList.remove('delete-mode');
            const deleteBtn = divider.querySelector('.delete-divider-btn');
            if (deleteBtn) deleteBtn.style.display = 'none';
        });
    }
}

function enableDragAndDrop(container) {
    const items = container.querySelectorAll('.book-card, .shelf-divider');
    items.forEach(item => {
        item.setAttribute('draggable', true);
        item.classList.add('sortable');
        
        // イベントリスナーをリセット
        item.removeEventListener('dragstart', handleDragStart);
        item.removeEventListener('dragend', handleDragEnd);
        item.removeEventListener('dragover', handleDragOver);
        item.removeEventListener('drop', handleDrop);
        
        // 新しいイベントリスナーを追加
        item.addEventListener('dragstart', handleDragStart);
        item.addEventListener('dragend', handleDragEnd);
        item.addEventListener('dragover', handleDragOver);
        item.addEventListener('drop', handleDrop);
        
        // クリックイベントを完全に無効化
        const link = item.querySelector('.book-link');
        if (link) {
            link.style.pointerEvents = 'none';
            item.style.pointerEvents = 'all';
        }
    });
}

function disableDragAndDrop(container) {
    const items = container.querySelectorAll('.book-card, .shelf-divider');
    items.forEach(item => {
        item.setAttribute('draggable', false);
        item.classList.remove('sortable');
        
        item.removeEventListener('dragstart', handleDragStart);
        item.removeEventListener('dragend', handleDragEnd);
        item.removeEventListener('dragover', handleDragOver);
        item.removeEventListener('drop', handleDrop);
        
        // クリックイベントを再度有効化
        const link = item.querySelector('.book-link');
        if (link) {
            link.style.pointerEvents = 'all';
            item.style.pointerEvents = 'all';
        }
    });
}

function handleDragStart(e) {
    e.stopImmediatePropagation();
    
    const item = e.target.closest('.book-card, .shelf-divider');
    if (!item || !item.classList.contains('sortable')) {
        e.preventDefault();
        return;
    }

    isDragging = true;

    // ドラッグ中の要素の構造を一時的に単純化
    if (item.classList.contains('book-card')) {
        // 元のHTML構造を保存
        item.dataset.originalHtml = item.innerHTML;
        
        // シンプルな構造に置き換え
        const title = item.querySelector('.book-title').textContent;
        item.innerHTML = `
            <div style="
                height: 100%;
                width: 100%;
                background: white !important;
                display: flex;
                align-items: center;
                writing-mode: vertical-rl;
                padding: 5px 2px;
                font-size: 0.8em;
            ">${title}</div>
        `;
    }
    
    item.classList.add('dragging');
    
    try {
        // 既存のドラッグデータ処理...
        const container = item.closest('.books-container');
        const itemData = {
            type: item.classList.contains('book-card') ? 'book' : 'divider',
            id: item.getAttribute(item.classList.contains('book-card') ? 'data-book-id' : 'data-divider-id'),
            originalShelfId: container.getAttribute('data-shelf-id'),
            originalPosition: item.getAttribute('data-position')
        };

        if (!itemData.id || !itemData.originalShelfId) {
            throw new Error('Required drag data missing');
        }

        e.dataTransfer.setData('application/json', JSON.stringify(itemData));
    } catch (error) {
        console.error('Drag start error:', error);
        e.preventDefault();
        isDragging = false;
        item.classList.remove('dragging');
    }
}

// グローバル変数としてスクロールのタイマーIDを保持
let scrollInterval = null;
const SCROLL_SPEED = 10; // スクロール速度（ピクセル/インターバル）
const SCROLL_THRESHOLD = 150; // スクロールを開始する画面端からの距離（ピクセル）

function handleDragOver(e) {
    e.preventDefault();
    e.stopImmediatePropagation();

    const draggingItem = document.querySelector('.dragging');
    if (!draggingItem) return;

    const container = e.target.closest('.books-container');
    if (!container || !container.classList.contains('sorting-mode')) return;

    // マウス位置を取得
    const mouseX = e.clientX;
    const mouseY = e.clientY;

    // ビューポートの高さを取得
    const viewportHeight = window.innerHeight;

    // スクロール処理
    handleScroll(mouseY, viewportHeight);

    // 最も近い要素を見つける処理（既存のコード）
    const items = [...container.querySelectorAll('.book-card:not(.dragging), .shelf-divider:not(.dragging)')];
    let closestItem = null;
    let closestDistance = Number.POSITIVE_INFINITY;

    items.forEach(item => {
        const box = item.getBoundingClientRect();
        const itemCenter = box.left + box.width / 2;
        const distance = Math.abs(mouseX - itemCenter);

        if (distance < closestDistance) {
            closestDistance = distance;
            closestItem = item;
        }
    });

    if (closestItem) {
        const box = closestItem.getBoundingClientRect();
        const insertAfter = mouseX > box.left + box.width / 2;

        if (insertAfter) {
            closestItem.insertAdjacentElement('afterend', draggingItem);
        } else {
            closestItem.insertAdjacentElement('beforebegin', draggingItem);
        }
    } else {
        container.appendChild(draggingItem);
    }

    updatePositions(container);
}

function handleScroll(mouseY, viewportHeight) {
    // スクロール開始位置の計算
    const topThreshold = SCROLL_THRESHOLD;
    const bottomThreshold = viewportHeight - SCROLL_THRESHOLD;

    // 既存のスクロールインターバルをクリア
    if (scrollInterval) {
        clearInterval(scrollInterval);
        scrollInterval = null;
    }

    // マウスが上端近くにある場合
    if (mouseY < topThreshold) {
        const speed = Math.max(1, (topThreshold - mouseY) / 10) * SCROLL_SPEED;
        scrollInterval = setInterval(() => {
            window.scrollBy(0, -speed);
        }, 16);
    }
    // マウスが下端近くにある場合
    else if (mouseY > bottomThreshold) {
        const speed = Math.max(1, (mouseY - bottomThreshold) / 10) * SCROLL_SPEED;
        scrollInterval = setInterval(() => {
            window.scrollBy(0, speed);
        }, 16);
    }
}

// ドラッグ終了時にスクロールを停止
function handleDragEnd(e) {
    e.preventDefault();
    isDragging = false;
    
    const item = e.target.closest('.book-card, .shelf-divider');
    if (!item) return;

    // スクロールを停止
    if (scrollInterval) {
        clearInterval(scrollInterval);
        scrollInterval = null;
    }

    // 元のHTML構造を復元
    if (item.classList.contains('book-card') && item.dataset.originalHtml) {
        item.innerHTML = item.dataset.originalHtml;
        delete item.dataset.originalHtml;
    }

    item.classList.remove('dragging');
    
    const newContainer = item.closest('.books-container');
    if (!newContainer) return;

    try {
        // データ取得を安全に行う
        const jsonData = e.dataTransfer.getData('application/json');
        if (!jsonData) {
            console.warn('No drag data found');
            return;
        }

        const dragData = JSON.parse(jsonData);
        if (!dragData || !dragData.originalShelfId) {
            console.warn('Invalid drag data format');
            return;
        }

        const newShelfId = newContainer.getAttribute('data-shelf-id');
        const originalShelfId = dragData.originalShelfId;

        if (newShelfId !== originalShelfId) {
            saveNewOrder(newContainer);
            const originalContainer = document.querySelector(
                `.books-container[data-shelf-id="${originalShelfId}"]`
            );
            if (originalContainer) {
                saveNewOrder(originalContainer);
            }
        } else {
            saveNewOrder(newContainer);
        }
    } catch (error) {
        console.error('Drag end error:', error);
        // エラーが発生した場合でもUIを正常な状態に戻す
        window.location.reload();
    }
}

// 位置情報を更新する補助関数
function updatePositions(container) {
    const items = Array.from(container.children);
    const updates = [];
    
    items.forEach((item, index) => {
        const oldPosition = parseInt(item.getAttribute('data-position'));
        if (oldPosition !== index) {
            item.setAttribute('data-position', index.toString());
            updates.push({ id: item.getAttribute('data-book-id') || item.getAttribute('data-divider-id'), position: index });
        }
    });
    
    return updates;
}

// これらのイベントリスナーをドキュメントレベルで設定
document.addEventListener('dragstart', handleDragStart, false);
document.addEventListener('dragover', handleDragOver, false);
document.addEventListener('dragend', handleDragEnd, false);

function handleDrop(e) {
    e.preventDefault();
}

function preventClick(e) {
    e.preventDefault();
}

async function saveNewOrder(container) {
    try {
        const items = Array.from(container.querySelectorAll('.book-card, .shelf-divider'));
        const shelfId = container.getAttribute('data-shelf-id');
        const bookPositions = [];
        const dividerPositions = [];

        // ポジションの計算を一度だけ行う
        items.forEach((item, index) => {
            if (item.classList.contains('book-card')) {
                bookPositions.push({
                    id: item.getAttribute('data-book-id'),
                    position: index,
                    shelfId: shelfId
                });
            } else {
                dividerPositions.push({
                    id: item.getAttribute('data-divider-id'),
                    position: index,
                    shelfId: shelfId
                });
            }
        });

        // 本と仕切りの位置を同時に更新
        const [bookResponse, dividerResponse] = await Promise.all([
            bookPositions.length > 0 ? fetch('/api/reorder', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Cache-Control': 'no-cache'
                },
                body: JSON.stringify({
                    shelfId: shelfId,
                    bookPositions: bookPositions
                })
            }) : Promise.resolve(),
            
            dividerPositions.length > 0 ? fetch('/api/dividers/reorder', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Cache-Control': 'no-cache'
                },
                body: JSON.stringify({
                    shelfId: shelfId,
                    dividerPositions: dividerPositions
                })
            }) : Promise.resolve()
        ]);

        // レスポンスの確認
        if ((bookPositions.length > 0 && !bookResponse.ok) || 
            (dividerPositions.length > 0 && !dividerResponse.ok)) {
            throw new Error('Failed to save positions');
        }

        return true;

    } catch (error) {
        console.error('Save order error:', error);
        
        // エラー発生時は現在のページをリロード
        window.location.reload();
        
        return false;
    }
}

function addNewDivider(container) {
    const label = prompt('仕切りのラベルを入力してください：');
    if (!label) return;

    const shelfId = container.getAttribute('data-shelf-id');
    const lastItem = container.querySelector('.book-card:last-child, .shelf-divider:last-child');
    const position = lastItem ? parseInt(lastItem.getAttribute('data-position')) + 1 : 0;

    fetch('/api/dividers', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            shelfId: shelfId,
            label: label,
            position: position
        })
    })
    .then(response => response.json())
    .then(divider => {
        const dividerElement = createDividerElement(divider);
        container.appendChild(dividerElement);
    })
    .catch(error => {
        console.error('Failed to add divider:', error);
        alert('仕切りの追加に失敗しました');
    });
}

function createDividerElement(divider) {
    const element = document.createElement('div');
    element.className = 'shelf-divider';
    element.setAttribute('data-divider-id', divider.id);
    element.setAttribute('data-position', divider.position);
    element.draggable = false;

    const labelElement = document.createElement('span');
    labelElement.className = 'divider-label';
    labelElement.textContent = divider.label;
    element.appendChild(labelElement);

    // 削除ボタン（デフォルトでは非表示）
    const deleteButton = document.createElement('button');
    deleteButton.className = 'delete-divider-btn';
    deleteButton.textContent = '×';
    deleteButton.style.display = 'none';  // デフォルトで非表示に設定
    deleteButton.setAttribute('data-divider-id', divider.id);
    element.appendChild(deleteButton);

    return element;
}

function deleteDivider(id, element) {
    fetch(`/api/dividers/${id}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            element.remove();
        } else {
            throw new Error('Failed to delete divider');
        }
    })
    .catch(error => {
        console.error('Error deleting divider:', error);
        alert('仕切りの削除に失敗しました');
    });
}




document.addEventListener('DOMContentLoaded', function() {
    console.log('Script loaded');  // スクリプトが読み込まれたことを確認
    
    const popup = document.getElementById('coverImagePopup');
    console.log('Popup element:', popup);  // ポップアップ要素が見つかるか確認
    
    const books = document.querySelectorAll('.book-card');
    console.log('Found books:', books.length);  // 本の要素が見つかるか確認
    
    books.forEach(book => {
        console.log('Book data:', {
            id: book.dataset.bookId,
            coverImage: book.dataset.coverImage
        });  // 各本のデータを確認
        
        book.addEventListener('mouseenter', function(e) {
            const coverImage = this.dataset.coverImage;
            console.log('Hover - Cover image:', coverImage);  // ホバー時の画像データを確認
            
            if (coverImage) {
                popup.querySelector('#popupImage').src = coverImage;
                popup.classList.remove('no-image');
            } else {
                popup.querySelector('#popupImage').src = '/images/default-cover.svg';
                popup.classList.remove('no-image');
            }
            
            const rect = this.getBoundingClientRect();
            popup.style.left = `${rect.right + 10}px`;
            popup.style.top = `${rect.top}px`;
            popup.style.display = 'block';
        });
    });
});


document.addEventListener('DOMContentLoaded', function() {
    const popup = document.getElementById('coverImagePopup');
    const popupImage = document.getElementById('popupImage');
    let activeBook = null;
    let isSortMode = false;  // ソートモードの状態を追跡

    function showPopup(book) {
        // ソートモード中はポップアップを表示しない
        if (isSortMode) return;

        const coverImage = book.dataset.coverImage;
        if (!popup || !popupImage) return;

        if (coverImage && coverImage !== 'null' && coverImage !== 'undefined') {
            popupImage.src = coverImage;
        } else {
            popupImage.src = '/images/default-book-cover.svg';
        }

        const rect = book.getBoundingClientRect();
        popup.style.left = `${rect.right + 20}px`;
        popup.style.top = `${rect.top}px`;
        popup.style.display = 'block';
        popup.classList.add('show');
        activeBook = book;
    }

    function hidePopup() {
        if (!popup) return;
        popup.classList.remove('show');
        activeBook = null;

        const onTransitionEnd = () => {
            if (!activeBook) {
                popup.style.display = 'none';
            }
            popup.removeEventListener('transitionend', onTransitionEnd);
        };
        popup.addEventListener('transitionend', onTransitionEnd);
    }

    // 順番入れ替えモードの切り替えを監視
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.target.classList.contains('sorting-mode')) {
                isSortMode = true;
                hidePopup();  // ソートモード開始時にポップアップを非表示
            } else {
                isSortMode = false;
            }
        });
    });

    // books-containerの監視を開始
    document.querySelectorAll('.books-container').forEach(container => {
        observer.observe(container, {
            attributes: true,
            attributeFilter: ['class']
        });
    });

    // 本のカードとその子要素のイベントを管理
    document.querySelectorAll('.book-card').forEach(book => {
        const handleEnter = () => {
            if (!isSortMode && activeBook !== book) {
                showPopup(book);
            }
        };

        const handleLeave = (e) => {
            if (isSortMode) return;  // ソートモード中は何もしない
            
            const relatedTarget = e.relatedTarget;
            if (relatedTarget && (book.contains(relatedTarget) || popup.contains(relatedTarget))) {
                return;
            }
            hidePopup();
        };

        book.addEventListener('mouseenter', handleEnter);
        book.addEventListener('mouseleave', handleLeave);
    });

    // ポップアップ自体のイベントを管理
    popup.addEventListener('mouseleave', (e) => {
        if (isSortMode) return;  // ソートモード中は何もしない

        const relatedTarget = e.relatedTarget;
        if (relatedTarget && activeBook && activeBook.contains(relatedTarget)) {
            return;
        }
        hidePopup();
    });

    // クリーンアップ
    return () => {
        observer.disconnect();
    };
});

//トップページ(本棚ページ)では「本の一覧」を非表示にする
document.addEventListener('DOMContentLoaded', function() {
    // 現在のパスを取得
    const currentPath = window.location.pathname;
    
    // トップページ（/）または/booksページの場合
    if (currentPath === '/' || currentPath === '/books') {
        // 「本の一覧」リンクを非表示
        const booksListLink = document.querySelector('a.nav-link[href="/books"]');
        if (booksListLink) {
            booksListLink.style.display = 'none';
        }
    }
});



    
document.addEventListener('DOMContentLoaded', function() {
    // ナビゲーションバーの順番入れ替えボタン
    const sortBooksNavBtn = document.querySelector('.sort-books-nav');
    if (sortBooksNavBtn) {
        sortBooksNavBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const bookshelf = document.querySelector('.bookshelf');
            const container = bookshelf.querySelector('.books-container');
            toggleSortMode(container, this);
        });
    }
});

document.addEventListener('DOMContentLoaded', function() {
    // トップページと本棚ページの判定
    const path = window.location.pathname;
    const isBookshelfPage = path === '/' || path === '/books';

    // 「本の一覧」リンクの制御
    const booksListLink = document.querySelector('a.nav-link[href="/books"]');
    if (booksListLink && isBookshelfPage) {
        // 「本の一覧」を非表示にする
        booksListLink.style.display = 'none';

        // 「順番入れ替え」ボタンを作成して追加
        const sortButton = document.createElement('button');
        sortButton.className = 'nav-link btn sort-books-nav';
        sortButton.textContent = '順番入れ替え';
        
        // 「本の登録」リンクの前に挿入
        const registerLink = document.querySelector('a.nav-link[href="/books/new"]');
        if (registerLink) {
            registerLink.parentNode.insertBefore(sortButton, registerLink);
        }

        // 順番入れ替えボタンのクリックイベント
        sortButton.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const bookshelf = document.querySelector('.bookshelf');
            if (bookshelf) {
                const container = bookshelf.querySelector('.books-container');
                if (container) {
                    toggleSortMode(container, this);
                    
                    // ボタンのテキストを更新
                    if (container.classList.contains('sorting-mode')) {
                        this.textContent = '順番入れ替えを完了';
                        this.classList.add('active');
                    } else {
                        this.textContent = '順番入れ替え';
                        this.classList.remove('active');
                    }
                }
            }
        });
    }
});

document.addEventListener('DOMContentLoaded', function() {
    let currentShelfId = null;
    const renameShelfModal = new bootstrap.Modal(document.getElementById('renameShelfModal'));
    
    // 本棚名変更ボタンのイベントリスナー
    document.querySelectorAll('.rename-shelf').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // クリックされた本棚のIDと現在の名前を保存
            currentShelfId = this.getAttribute('data-shelf-id');
            const currentName = this.getAttribute('data-shelf-name');
            
            // 現在の名前をフォームに設定
            document.getElementById('newShelfName').value = currentName;
            
            // モーダルを表示
            renameShelfModal.show();
            
            // ドロップダウンメニューを閉じる
            const dropdownMenu = this.closest('.dropdown-menu');
            const dropdownToggle = document.querySelector(`[aria-controls="${dropdownMenu.id}"]`);
            const dropdown = bootstrap.Dropdown.getInstance(dropdownToggle);
            if (dropdown) {
                dropdown.hide();
            }
        });
    });
    
    // 保存ボタンのイベントリスナー
    document.getElementById('saveRename').addEventListener('click', function() {
        const nameInput = document.getElementById('newShelfName');
        const newName = nameInput.value.trim();
        
        if (!newName) {
            alert('本棚の名前を入力してください。');
            return;
        }

        fetch(`/api/shelves/${currentShelfId}/rename`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: newName
            })
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || '本棚名の変更に失敗しました');
                });
            }
            // レスポンスの処理が完了してから、モーダルを閉じてリロード
            renameShelfModal.hide();
            nameInput.value = '';
            // 少し遅延を入れてリロード
            setTimeout(() => {
                window.location.reload();
            }, 100);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('本棚名の変更に失敗しました。もう一度お試しください。');
        });
    });

    // モーダルが閉じられたときにフォームをリセット
    document.getElementById('renameShelfModal').addEventListener('hidden.bs.modal', function () {
        document.getElementById('newShelfName').value = '';
        currentShelfId = null;
    });
});

// カスタマイズ機能
document.addEventListener('DOMContentLoaded', function() {
    const customizeButtons = document.querySelectorAll('.customize-shelf');
    
    // ページ読み込み時に保存された状態を復元
    restoreCustomization();

    customizeButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const allBookshelves = document.querySelectorAll('.bookshelf');
            const firstBookshelf = allBookshelves[0];
            
            if (allBookshelves.length > 0) {
                let newMode = '';

                if (!firstBookshelf.classList.contains('dark-mode') && 
                    !firstBookshelf.classList.contains('cream-mode')) {
                    // 通常→濃い茶色
                    newMode = 'dark-mode';
                    allBookshelves.forEach(shelf => {
                        shelf.classList.add('dark-mode');
                    });
                } else if (firstBookshelf.classList.contains('dark-mode')) {
                    // 濃い茶色→クリーム色
                    newMode = 'cream-mode';
                    allBookshelves.forEach(shelf => {
                        shelf.classList.remove('dark-mode');
                        shelf.classList.add('cream-mode');
                    });
                } else {
                    // クリーム色→通常
                    newMode = '';
                    allBookshelves.forEach(shelf => {
                        shelf.classList.remove('cream-mode');
                    });
                }

                // 現在の状態を保存
                localStorage.setItem('bookshelfMode', newMode);
            }

            // ドロップダウンメニューを閉じる
            const dropdownMenu = this.closest('.dropdown-menu');
            if (dropdownMenu) {
                const dropdown = bootstrap.Dropdown.getInstance(dropdownMenu.previousElementSibling);
                if (dropdown) {
                    dropdown.hide();
                }
            }
        });
    });
});

// 保存された状態を復元する関数
function restoreCustomization() {
    const savedMode = localStorage.getItem('bookshelfMode');
    if (savedMode) {
        const allBookshelves = document.querySelectorAll('.bookshelf');
        allBookshelves.forEach(shelf => {
            // 一旦全てのモードをクリア
            shelf.classList.remove('dark-mode', 'cream-mode');
            // 保存されていたモードを適用
            if (savedMode) {
                shelf.classList.add(savedMode);
            }
        });
    }
}



document.addEventListener('DOMContentLoaded', function() {
    // タブの切り替えイベントをリッスン
    const tabElements = document.querySelectorAll('button[data-bs-toggle="tab"]');
    
    tabElements.forEach(tab => {
        tab.addEventListener('shown.bs.tab', function (event) {
            const container = document.querySelector('.container.mt-5');
            if (event.target.id === 'login-tab') {
                container.style.minHeight = '120vh';
            } else if (event.target.id === 'register-tab') {
                container.style.minHeight = '150vh';
            }
        });
    });

    // 初期状態の設定
    const container = document.querySelector('.container.mt-5');
    if (document.querySelector('#login-tab').classList.contains('active')) {
        container.style.minHeight = '120vh';
    }
});






function startDragging(item) {
    isDragging = true;
    item.classList.add('dragging');

    // Swipe Navigation防止用のレイヤーを追加
    const swipeDisabler = document.createElement('div');
    swipeDisabler.className = 'swipe-disable-layer';
    document.body.appendChild(swipeDisabler);

    // スタイルを追加
    swipeDisabler.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: 999;
        background: transparent;
        touch-action: none;
    `;
}

function stopDragging(item) {
    isDragging = false;
    item.classList.remove('dragging');

    // Swipe Navigation防止用のレイヤーを削除
    const swipeDisabler = document.querySelector('.swipe-disable-layer');
    if (swipeDisabler) {
        swipeDisabler.remove();
    }
}
