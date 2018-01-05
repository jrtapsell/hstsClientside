self.addEventListener('fetch', function(event) {
    var request = event.request;
    event.respondWith(
        caches.open('mysite-dynamic').then(function(cache) {
            return cache.match(request).then(function (response) {
                return response || fetch(request).then(function(response) {
                    if (request.headers.get("permaCache")) {
                        cache.put(event.request, response.clone());
                    }
                    return response;
                });
            });
        })
    );
});
