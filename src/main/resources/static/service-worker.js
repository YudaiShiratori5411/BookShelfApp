const CACHE_NAME = 'bookshelf-v1';
const urlsToCache = [
  '/',
  '/login',
  '/css/bookshelf.css',
  '/css/auth-tabs.css',
  '/css/orientation.css',
  '/css/reading-count.css',
  '/css/dropdown.css',
  '/css/shelf-divider.css',
  '/css/search-bar.css',
  '/js/search-bar.js',
  '/js/image-upload.js',
  '/js/scanner/BookFormScanner.js',
  '/js/reading-count.js',
  '/icons/icon-192x192.png',
  '/icons/icon-512x512.png'
];

// インストール時の処理
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

// フェッチ時の処理
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // キャッシュが見つかればそれを返す
        if (response) {
          return response;
        }
        // 見つからなければネットワークへ取りに行く
        return fetch(event.request);
      })
  );
});
