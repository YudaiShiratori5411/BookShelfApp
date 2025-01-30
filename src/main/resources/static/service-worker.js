const CACHE_NAME = 'bookshelf-v1';
const urlsToCache = [
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
    '/css/style.css',
    '/js/bookshelf.js',
    '/js/dropdown.js',
    '/js/form-validation.js',
    '/js/image-upload.js',
    '/js/reading-count.js',
    '/js/reading-progress-chart.js',
    '/js/readingSession.js',
    '/js/search-bar.js',
    '/js/scanner/BookFormScanner.js',
    '/icons/icon-192x192.png',
    '/icons/icon-512x512.png',
    '/manifest.json',
    '/favicon.ico'
];

// インストール時の処理
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('Opening cache...');
                return cache.addAll(urlsToCache)
                    .catch(error => {
                        console.error('Cache addAll error:', error);
                        // 個別のリソースのキャッシュを試みる
                        return Promise.all(
                            urlsToCache.map(url =>
                                cache.add(url).catch(err => {
                                    console.error('Failed to cache:', url, err);
                                })
                            )
                        );
                    });
            })
    );
});

// フェッチ時の処理
self.addEventListener('fetch', event => {
    // CSSファイルの場合はキャッシュを使用せず、常にネットワークから取得
    if (event.request.url.endsWith('.css')) {
        event.respondWith(
            fetch(event.request)
                .catch(() => {
                    return caches.match(event.request);
                })
        );
        return;
    }

    event.respondWith(
        caches.match(event.request)
            .then(response => {
                return response || fetch(event.request);
            })
    );
});

// アクティベーション時の処理（古いキャッシュの削除）
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    if (cacheName !== CACHE_NAME) {
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
});
