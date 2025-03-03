(function() {
  console.log('Final profile image solution loaded');
  
  // ページロード前の即時実行
  (function() {
    try {
      // URLからユーザーIDを取得
      const urlMatch = location.pathname.match(/\/users\/profile\/(\d+)/);
      const userId = urlMatch ? urlMatch[1] : localStorage.getItem('currentUserId');
      
      if (userId && localStorage.getItem('profileUpdated_' + userId) === 'true') {
        const savedImage = localStorage.getItem('profileImage_' + userId);
        if (savedImage) {
          console.log('Preloading image for user', userId);
          
          // DOMが準備できる前に画像更新を開始
          const checkAndUpdateInterval = setInterval(function() {
            const headerImg = document.querySelector('#currentUserImage');
            if (headerImg) {
              headerImg.src = savedImage;
              console.log('Header image preloaded');
              clearInterval(checkAndUpdateInterval);
            }
          }, 10);
          
          // 安全対策としてタイムアウト設定
          setTimeout(function() {
            clearInterval(checkAndUpdateInterval);
          }, 3000);
        }
      }
    } catch (e) {
      console.error('Error in preload:', e);
    }
  })();
  
  // メイン処理
  document.addEventListener('DOMContentLoaded', function() {
    // ユーザー情報取得
    const currentUserId = getCurrentUserId();
    console.log('Current user ID:', currentUserId);

    // プロフィールページの場合
    if (document.getElementById('profileImage')) {
      setupProfileImageUpload(currentUserId);
      handleFormSubmission(currentUserId);
    } else {
      // 通常ページの場合、user-selector.jsとの競合を避けるためにMutationObserverを使用
      monitorAndProtectHeaderImage(currentUserId);
    }
    
    // (1) ユーザーID取得の優先順位
    function getCurrentUserId() {
      // URLから取得
      const urlMatch = location.pathname.match(/\/users\/profile\/(\d+)/);
      if (urlMatch) return urlMatch[1];
      
      // データ属性から取得
      const headerImg = document.querySelector('#currentUserImage');
      if (headerImg && headerImg.dataset.userId) {
        return headerImg.dataset.userId;
      }
      
      // ユーザー名要素から取得
      const userNameEl = document.querySelector('#currentUserName');
      if (userNameEl && userNameEl.dataset.userId) {
        return userNameEl.dataset.userId;
      }
      
      // LocalStorageから取得（最も信頼性が低い）
      return localStorage.getItem('currentUserId');
    }
    
    // (2) プロフィール画像のアップロード処理
    function setupProfileImageUpload(userId) {
      if (!userId) return;
      
      const fileInput = document.getElementById('profileImage');
      if (!fileInput) return;
      
      console.log('Setting up profile image upload handler');
      
      fileInput.addEventListener('change', function() {
        if (this.files && this.files[0]) {
          const reader = new FileReader();
          
          reader.onload = function(e) {
            const imagePath = e.target.result;
            
            // プロフィール画像更新
            const profileImg = document.querySelector('.profile-image');
            if (profileImg) profileImg.src = imagePath;
            
            // ヘッダー画像更新
            const headerImg = document.querySelector('#currentUserImage');
            if (headerImg) headerImg.src = imagePath;
            
            // LocalStorageに保存（user-selector.jsとの連携）
            localStorage.setItem('currentUserImage', imagePath);
            
            // ユーザーID別の保存（競合回避）
            localStorage.setItem('profileImage_' + userId, imagePath);
            localStorage.setItem('profileUpdated_' + userId, 'true');
            localStorage.setItem('profileUpdateTime_' + userId, Date.now());
            
            console.log('Image updated and saved for user', userId);
          };
          
          reader.readAsDataURL(this.files[0]);
        }
      });
    }
    
    // (3) フォーム送信時の処理
    function handleFormSubmission(userId) {
      if (!userId) return;
      
      const form = document.querySelector('form');
      if (!form) return;
      
      form.addEventListener('submit', function() {
        try {
          // 現在の画像を保存
          const profileImg = document.querySelector('.profile-image');
          if (profileImg) {
            const imagePath = profileImg.src;
            
            // ユーザー別保存
            localStorage.setItem('profileImage_' + userId, imagePath);
            localStorage.setItem('profileUpdated_' + userId, 'true');
            localStorage.setItem('profileUpdateTime_' + userId, Date.now());
            
            // 汎用保存（user-selector.jsとの連携）
            localStorage.setItem('currentUserImage', imagePath);
            
            console.log('Image saved on form submit for user', userId);
          }
        } catch (e) {
          console.error('Error saving image on form submit:', e);
        }
      });
    }
    
    // (4) ヘッダー画像の監視と保護
    function monitorAndProtectHeaderImage(userId) {
      if (!userId) return;
      
      // 保存済み画像の確認
      const savedImage = localStorage.getItem('profileImage_' + userId);
      const isUpdated = localStorage.getItem('profileUpdated_' + userId) === 'true';
      
      if (!isUpdated || !savedImage) return;
      
      // 即時更新
      const headerImg = document.querySelector('#currentUserImage');
      if (headerImg) {
        headerImg.src = savedImage;
        console.log('Header image restored');
      }
      
      // 遅延更新（他のスクリプトが上書きした場合のバックアップ）
      setTimeout(function() {
        const headerImg = document.querySelector('#currentUserImage');
        if (headerImg && headerImg.src !== savedImage) {
          headerImg.src = savedImage;
          console.log('Header image re-restored after delay');
        }
      }, 100);
      
      // MutationObserverによる継続的監視
      if (window.MutationObserver) {
        const headerImg = document.querySelector('#currentUserImage');
        if (!headerImg) return;
        
        const observer = new MutationObserver(function(mutations) {
          mutations.forEach(function(mutation) {
            if (mutation.type === 'attributes' && 
                mutation.attributeName === 'src' && 
                headerImg.src !== savedImage) {
              
              console.log('Header image changed by another script, restoring...');
              setTimeout(function() {
                headerImg.src = savedImage;
              }, 10);
            }
          });
        });
        
        observer.observe(headerImg, {
          attributes: true,
          attributeFilter: ['src']
        });
        
        console.log('Mutation observer setup for header image protection');
      }
    }
  });
  
  // グローバル関数 - 他のスクリプトから画像を更新できるようにする
  window.globalUpdateUserImage = function(userId, imagePath) {
    if (!userId || !imagePath) return false;
    
    try {
      // ユーザー別保存
      localStorage.setItem('profileImage_' + userId, imagePath);
      localStorage.setItem('profileUpdated_' + userId, 'true');
      localStorage.setItem('profileUpdateTime_' + userId, Date.now());
      
      // 汎用保存（user-selector.jsとの連携）
      localStorage.setItem('currentUserImage', imagePath);
      
      // 現在のユーザーの場合はヘッダーも更新
      const headerImg = document.querySelector('#currentUserImage');
      const currentUserId = localStorage.getItem('currentUserId');
      
      if (headerImg && userId === currentUserId) {
        headerImg.src = imagePath;
      }
      
      return true;
    } catch (e) {
      console.error('Error in globalUpdateUserImage:', e);
      return false;
    }
  };
})();