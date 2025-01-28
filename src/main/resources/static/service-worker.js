const CACHE_NAME = 'bookshelf-v1';
const urlsToCache = [
    '/',
    '/login',
    '/css/components/book-card.css',
    '/css/components/cover-image.css',
    '/css/components/forms.css',
    '/css/components/layout.css',
    '/css/components/navigation.css',
    '/css/components/shelf.css',
    '/css/components/sorting.css',
    '/css/auth-tabs.css',
    '/css/dropdown.css',
    '/css/orientation.css',
    '/css/reading-count.css',
    '/css/search-bar.css',
    '/css/shelf-divider.css',
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
            .then(cache => {
                console.log('Opening cache...');
                const cachePromises = urlsToCache.map(url => {
                    return cache.add(url).catch(err => {
                        console.error('Error caching', url, err);
                    });
                });
                return Promise.all(cachePromises);
            })
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
