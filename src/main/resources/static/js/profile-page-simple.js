document.addEventListener('DOMContentLoaded', function() {
  console.log('Enhanced profile management v2 loaded');
  
  // 重要な要素の取得
  const profileForm = document.querySelector('form');
  const fileInput = document.getElementById('profileImage');
  const profileImg = document.querySelector('.profile-image');
  const headerImg = document.querySelector('#currentUserImage');
  
  // URLからユーザーIDを取得
  const urlMatch = location.pathname.match(/\/users\/profile\/(\d+)/);
  const userId = urlMatch ? urlMatch[1] : null;
  console.log('Current user ID from URL:', userId);
  
  if (!userId) return;
  
  // データURLが有効かどうかをチェックする関数
  function isValidDataUrl(url) {
    if (!url) return false;
    return url.startsWith('data:image/') && url.includes('base64');
  }
  
  // ファイルURLが有効かどうかをチェックする関数
  function isValidFileUrl(url) {
    if (!url) return false;
    return url.startsWith('http') && (url.includes('/uploads/profiles/') || url.includes('/images/'));
  }
  
  // 最も信頼できる画像URLを取得
  function getBestImageUrl() {
    // 1. プロフィール画像がある場合はそれを使用
    if (profileImg && isValidFileUrl(profileImg.src)) {
      return profileImg.src;
    }
    
    // 2. ヘッダー画像が有効な場合はそれを使用
    if (headerImg && (isValidFileUrl(headerImg.src) || isValidDataUrl(headerImg.src))) {
      return headerImg.src;
    }
    
    // 3. LocalStorageに保存された画像がある場合はそれを使用
    const storedImage = localStorage.getItem('profileImage_' + userId);
    if (storedImage && (isValidFileUrl(storedImage) || isValidDataUrl(storedImage))) {
      return storedImage;
    }
    
    // 4. どれも使えない場合はデフォルト画像を返す
    return '/images/default-profile.jpg';
  }
  
  // 現在のヘッダー画像を保存
  const originalHeaderSrc = headerImg ? headerImg.src : null;
  console.log('Original header image source:', originalHeaderSrc);
  
  // ヘッダー画像の保護設定
  if (headerImg && (isValidFileUrl(originalHeaderSrc) || isValidDataUrl(originalHeaderSrc))) {
    // 1. MutationObserverによる継続的監視
    if (window.MutationObserver) {
      const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
          if (mutation.type === 'attributes' && 
              mutation.attributeName === 'src' && 
              headerImg.src !== originalHeaderSrc &&
              // 重要: 新しいURLが無効な場合のみ復元する
              (!isValidFileUrl(headerImg.src) && !isValidDataUrl(headerImg.src))) {
            
            console.log('Invalid header image detected, restoring to original');
            headerImg.src = originalHeaderSrc;
          }
        });
      });
      
      observer.observe(headerImg, {
        attributes: true,
        attributeFilter: ['src']
      });
      
      console.log('Mutation observer setup to protect header image');
    }
    
    // 2. LocalStorageを更新して他のスクリプトとの整合性を保つ
    if (isValidFileUrl(originalHeaderSrc) || isValidDataUrl(originalHeaderSrc)) {
      localStorage.setItem('profileImage_' + userId, originalHeaderSrc);
      localStorage.setItem('currentUserImage', originalHeaderSrc);
      console.log('LocalStorage updated with current header image');
    }
    
    // 3. 遅延チェックで画像が変更されていないか確認
    setTimeout(function() {
      if (headerImg.src !== originalHeaderSrc) {
        console.log('Delayed check: header image changed, restoring');
        headerImg.src = originalHeaderSrc;
      }
    }, 500);
  }
  
  // ファイル選択時の処理
  if (fileInput) {
    fileInput.addEventListener('change', function() {
      if (this.files && this.files[0]) {
        const reader = new FileReader();
        
        reader.onload = function(e) {
          const imageDataUrl = e.target.result;
          if (!isValidDataUrl(imageDataUrl)) {
            console.error('Invalid image data URL');
            return;
          }
          
          // プロフィール画像を更新
          if (profileImg) {
            profileImg.src = imageDataUrl;
            console.log('Profile image preview updated');
          }
          
          // 一時データとして保存
          if (userId) {
            localStorage.setItem('temp_profileImage_' + userId, imageDataUrl);
            console.log('Temporary profile image saved');
          }
        };
        
        reader.readAsDataURL(this.files[0]);
      }
    });
  }
  
  // フォーム送信時の処理
  if (profileForm && userId) {
    profileForm.addEventListener('submit', function(e) {
      console.log('Form submission detected');
      
      // 現在表示されている画像の正規化
      const currentImg = profileImg ? profileImg.src : null;
      if (currentImg && (isValidFileUrl(currentImg) || isValidDataUrl(currentImg))) {
        // サーバーサイドでの処理を待つため、フラグのみ設定
        localStorage.setItem('profileUpdated_' + userId, 'true');
        localStorage.setItem('profileUpdateTime_' + userId, Date.now());
      }
    });
  }
  
  // 戻るボタン処理
  const backButton = document.getElementById('backButton');
  if (backButton) {
    backButton.addEventListener('click', function() {
      if (userId) {
        // 一時データをクリア
        localStorage.removeItem('temp_profileImage_' + userId);
        
        // 画像更新をスキップするフラグを設定
        localStorage.setItem('skipImageRefresh', 'true');
        console.log('Back button clicked, temp data cleared');
      }
    });
  }
});