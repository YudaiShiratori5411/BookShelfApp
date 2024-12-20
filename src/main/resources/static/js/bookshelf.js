//let draggedElement = null;
//
//function handleDragStart(event) {
//    draggedElement = event.target;
//    event.target.classList.add('dragging');
//    event.dataTransfer.effectAllowed = 'move';
//    event.dataTransfer.setData('text/plain', event.target.dataset.bookId);
//}
//
//function handleDragEnd(event) {
//    event.target.classList.remove('dragging');
//    draggedElement = null;
//    
//    // すべてのドロップ領域のスタイルをリセット
//    document.querySelectorAll('.books-container').forEach(container => {
//        container.classList.remove('drag-over');
//    });
//}
//
//function handleDragOver(event) {
//    event.preventDefault();
//    event.dataTransfer.dropEffect = 'move';
//    event.currentTarget.classList.add('drag-over');
//}
//
//function handleDragLeave(event) {
//    event.currentTarget.classList.remove('drag-over');
//}
//
//function handleDrop(event) {
//    event.preventDefault();
//    const bookId = event.dataTransfer.getData('text/plain');
//    const newShelfId = event.currentTarget.dataset.shelfId;
//    
//    // ドロップ領域のスタイルをリセット
//    event.currentTarget.classList.remove('drag-over');
//
//    // 同じ本棚内での移動の場合は位置を更新
//    if (draggedElement && event.currentTarget.contains(draggedElement.parentNode)) {
//        const { clientX } = event;
//        const books = Array.from(event.currentTarget.children);
//        const insertAfter = books.find(book => {
//            const rect = book.getBoundingClientRect();
//            return clientX < rect.left + rect.width / 2;
//        });
//        
//        if (insertAfter) {
//            event.currentTarget.insertBefore(draggedElement, insertAfter);
//        } else {
//            event.currentTarget.appendChild(draggedElement);
//        }
//    } else {
//        // 異なる本棚への移動
//        fetch(`/books/${bookId}/move?shelfId=${newShelfId}`, {
//            method: 'POST',
//            headers: {
//                'Content-Type': 'application/json',
//            }
//        })
//        .then(response => {
//            if (!response.ok) throw new Error('本の移動に失敗しました');
//            window.location.reload();
//        })
//        .catch(error => {
//            console.error('Error:', error);
//            alert(error.message);
//        });
//    }
//}