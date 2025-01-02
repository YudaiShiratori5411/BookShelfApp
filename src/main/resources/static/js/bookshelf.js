document.addEventListener('DOMContentLoaded', function() {
    console.log('Script loaded');

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
    const isInSortMode = container.classList.toggle('sorting-mode');

    if (isInSortMode) {
        button.textContent = '順番入れ替えを完了';
        enableDragAndDrop(container);
    } else {
        button.textContent = '順番入れ替え';
        disableDragAndDrop(container);
        saveNewOrder(container);
        // '順番入れ替えを完了'クリック時に自動リロード
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
        item.addEventListener('dragstart', handleDragStart);
        item.addEventListener('dragend', handleDragEnd);
        item.addEventListener('dragover', handleDragOver);
        item.addEventListener('drop', handleDrop);
        
        const link = item.querySelector('.book-link');
        if (link) {
            link.addEventListener('click', preventClick);
        }
    });
}

function disableDragAndDrop(container) {
    const items = container.querySelectorAll('.book-card, .shelf-divider');
    items.forEach(item => {
        item.setAttribute('draggable', false);  // falseに変更
        item.removeEventListener('dragstart', handleDragStart);
        item.removeEventListener('dragend', handleDragEnd);
        item.removeEventListener('dragover', handleDragOver);
        item.removeEventListener('drop', handleDrop);
        
        const link = item.querySelector('.book-link');
        if (link) {
            link.removeEventListener('click', preventClick);
        }
    });
}

function handleDragStart(e) {
    const item = e.target.closest('.book-card, .shelf-divider');
    if (item) {
        item.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'move';
        
        // 元の位置を保存
        item.setAttribute('data-original-position', item.getAttribute('data-position'));
        
        const itemType = item.classList.contains('book-card') ? 'book' : 'divider';
        const itemId = itemType === 'book' ? 
            item.getAttribute('data-book-id') : 
            item.getAttribute('data-divider-id');
            
        console.log('Drag started:', {
            type: itemType,
            id: itemId,
            originalPosition: item.getAttribute('data-original-position')
        });

        e.dataTransfer.setData('application/json', JSON.stringify({
            type: itemType,
            id: itemId,
            originalPosition: item.getAttribute('data-original-position')
        }));
    }
}

function handleDragOver(e) {
    e.preventDefault();
    e.stopPropagation();

    const draggingItem = document.querySelector('.dragging');
    const item = e.target.closest('.book-card, .shelf-divider');
    const container = item?.parentElement;
    
    if (draggingItem && item && container && draggingItem !== item) {
        const rect = item.getBoundingClientRect();
        const middle = rect.left + rect.width / 2;
        
        if (e.clientX < middle) {
            container.insertBefore(draggingItem, item);
        } else {
            container.insertBefore(draggingItem, item.nextSibling);
        }

        // position属性を更新
        updatePositions(container);
    }
}

// 位置情報を更新する補助関数
function updatePositions(container) {
    const items = Array.from(container.children);
    items.forEach((item, index) => {
        item.setAttribute('data-position', index.toString());
    });
}

function handleDragEnd(e) {
    e.preventDefault();
    e.stopPropagation();  // 追加

    const item = e.target.closest('.book-card, .shelf-divider');
    if (item) {
        item.classList.remove('dragging');
        
        // 並び順を保存
        const container = item.closest('.books-container');
        if (container) {
            console.log('Saving new order after drag end');
            saveNewOrder(container);
        }
    }
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

function saveNewOrder(container) {
    const items = Array.from(container.querySelectorAll('.book-card, .shelf-divider'));
    const bookPositions = [];
    const dividerPositions = [];
    
    // 位置の再計算を行う
    items.forEach((item, index) => {
        if (item.classList.contains('shelf-divider')) {
            dividerPositions.push({
                id: item.getAttribute('data-divider-id'),
                position: index
            });
        } else if (item.classList.contains('book-card')) {
            bookPositions.push({
                id: item.getAttribute('data-book-id'),
                position: index
            });
        }
    });

    const shelfId = container.getAttribute('data-shelf-id');
    
    console.log('Attempting to save order:', {
        shelfId: shelfId,
        bookPositions: bookPositions,
        dividerPositions: dividerPositions
    });

    // 保存処理を1つのトランザクションとして扱う
    const savePositions = async () => {
        try {
            // 両方の位置情報を同時に送信
            const responses = await Promise.all([
                // 仕切りの位置を保存
                dividerPositions.length > 0 ? 
                    fetch('/api/dividers/reorder', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Cache-Control': 'no-cache'
                        },
                        body: JSON.stringify({
                            shelfId: shelfId,
                            dividerPositions: dividerPositions
                        })
                    }) : Promise.resolve(),
                
                // 本の位置を保存
                bookPositions.length > 0 ?
                    fetch('/api/reorder', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Cache-Control': 'no-cache'
                        },
                        body: JSON.stringify({
                            shelfId: shelfId,
                            bookPositions: bookPositions
                        })
                    }) : Promise.resolve()
            ]);

            // レスポンスの検証
            for (const response of responses) {
                if (response && !response.ok) {
                    throw new Error(`Server responded with ${response.status}`);
                }
            }

            console.log('Successfully saved all positions');

        } catch (error) {
            console.error('Error saving positions:', error);
            
            // エラー発生時の復旧処理
            try {
                // 元の位置情報を保持
                const originalOrder = Array.from(container.children).map(item => ({
                    element: item,
                    position: parseInt(item.getAttribute('data-position'))
                }));

                // 要素を元の位置に戻す
                originalOrder.sort((a, b) => a.position - b.position);
                originalOrder.forEach(item => container.appendChild(item.element));
                
                alert('並び順の保存に失敗しました。元の順序に戻します。');
            } catch (recoveryError) {
                console.error('Recovery failed:', recoveryError);
                alert('並び順の保存に失敗し、復旧も失敗しました。ページをリロードしてください。');
                window.location.reload();
            }
        }
    };

    // 保存処理の実行
    savePositions();
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


