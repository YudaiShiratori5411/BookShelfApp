// URLからbook_idを取得する関数
function getBookIdFromUrl() {
  const path = window.location.pathname;
  const matches = path.match(/\/books\/(\d+)/);
  return matches ? matches[1] : null;
}

function decrementCount() {
  const countElement = document.getElementById('readingCount');
  let count = parseInt(countElement.textContent);
  if (count > 0) {
    countElement.textContent = count - 1;
    saveCountToStorage(count - 1);
  }
}

function incrementCount() {
  const countElement = document.getElementById('readingCount');
  let count = parseInt(countElement.textContent);
  countElement.textContent = count + 1;
  saveCountToStorage(count + 1);
}

function saveCountToStorage(count) {
  const bookId = getBookIdFromUrl();
  if (bookId) {
    localStorage.setItem(`book_${bookId}_reading_count`, count);
  }
}

function initializeReadingCount() {
  const bookId = getBookIdFromUrl();
  if (!bookId) return;

  const countElement = document.getElementById('readingCount');
  if (!countElement) return;

  const savedCount = localStorage.getItem(`book_${bookId}_reading_count`);
  countElement.textContent = savedCount !== null ? savedCount : '1';
}

// DOMContentLoadedイベントで初期化とイベントリスナーの設定を行う
document.addEventListener('DOMContentLoaded', function() {
  initializeReadingCount();

  // 減少ボタンのイベントリスナー
  const decreaseBtn = document.querySelector('.decrease-count');
  if (decreaseBtn) {
    decreaseBtn.addEventListener('click', decrementCount);
  }

  // 増加ボタンのイベントリスナー
  const increaseBtn = document.querySelector('.increase-count');
  if (increaseBtn) {
    increaseBtn.addEventListener('click', incrementCount);
  }
});