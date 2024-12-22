document.addEventListener('DOMContentLoaded', function() {
    console.log('Script loaded');

    // Bootstrapのドロップダウンを初期化
    var dropdownElementList = [].slice.call(document.querySelectorAll('[data-bs-toggle="dropdown"]'))
    var dropdownList = dropdownElementList.map(function (dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl)
    });
    console.log('Dropdowns initialized:', dropdownList.length);

    // 順番入れ替えボタンのイベントリスナー
    document.querySelectorAll('.sort-books').forEach(button => {
        console.log('Sort button found:', button);
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();  // イベントの伝播を停止
            console.log('Sort button clicked');
            const bookshelf = this.closest('.bookshelf');
            const container = bookshelf.querySelector('.books-container');
            toggleSortMode(container, this);

            // ドロップダウンメニューを閉じる
            const dropdownMenu = this.closest('.dropdown-menu');
            const dropdownToggle = document.querySelector(`[aria-controls="${dropdownMenu.id}"]`);
            const dropdown = bootstrap.Dropdown.getInstance(dropdownToggle);
            if (dropdown) {
                dropdown.hide();
            }
        });
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
    }
}

function enableDragAndDrop(container) {
    const bookCards = container.querySelectorAll('.book-card');
    bookCards.forEach(card => {
        card.setAttribute('draggable', true);
        card.addEventListener('dragstart', handleDragStart);
        card.addEventListener('dragend', handleDragEnd);
        card.addEventListener('dragover', handleDragOver);
        card.addEventListener('drop', handleDrop);
        
        // リンクのクリックを一時的に無効化
        const link = card.querySelector('.book-link');
        if (link) {
            link.addEventListener('click', preventClick);
        }
    });
}

function disableDragAndDrop(container) {
    const bookCards = container.querySelectorAll('.book-card');
    bookCards.forEach(card => {
        card.removeAttribute('draggable');
        card.removeEventListener('dragstart', handleDragStart);
        card.removeEventListener('dragend', handleDragEnd);
        card.removeEventListener('dragover', handleDragOver);
        card.removeEventListener('drop', handleDrop);
        
        // リンクのクリックを再度有効化
        const link = card.querySelector('.book-link');
        if (link) {
            link.removeEventListener('click', preventClick);
        }
    });
}

function handleDragStart(e) {
    const bookCard = e.target.closest('.book-card');
    if (bookCard) {
        bookCard.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', bookCard.getAttribute('data-book-id'));
    }
}

function handleDragOver(e) {
    e.preventDefault();
    const draggingCard = document.querySelector('.book-card.dragging');
    const card = e.target.closest('.book-card');
    const container = card?.parentElement;
    
    if (draggingCard && card && container && draggingCard !== card) {
        const rect = card.getBoundingClientRect();
        const middle = rect.left + rect.width / 2;
        
        if (e.clientX < middle) {
            container.insertBefore(draggingCard, card);
        } else {
            container.insertBefore(draggingCard, card.nextSibling);
        }
    }
}

function handleDragEnd(e) {
    const bookCard = e.target.closest('.book-card');
    if (bookCard) {
        bookCard.classList.remove('dragging');
        // 並び順を保存
        const container = bookCard.closest('.books-container');
        if (container) {
            saveNewOrder(container);
        }
    }
}

function handleDrop(e) {
    e.preventDefault();
}

function preventClick(e) {
    e.preventDefault();
}

function saveNewOrder(container) {
    const bookIds = Array.from(container.querySelectorAll('.book-card'))
        .map(card => card.getAttribute('data-book-id'));
    const shelfId = container.getAttribute('data-shelf-id');
    
    console.log('Sending reorder request:', {
        shelfId: shelfId,
        bookIds: bookIds
    });

    fetch('/api/books/reorder', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            shelfId: shelfId,
            bookIds: bookIds
        })
    })
    .then(async response => {
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.error || '順序の保存に失敗しました');
        }
        console.log('Reorder success:', data);
        return data;
    })
    .catch(error => {
        console.error('Reorder error:', error);
        alert('順序の保存に失敗しました：' + error.message);
    });
}




