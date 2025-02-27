document.addEventListener('DOMContentLoaded', function() {
    // DOM要素
    const userDropdownMenu = document.getElementById('userDropdownMenu');
    const currentUserImage = document.getElementById('currentUserImage');
    const currentUserName = document.getElementById('currentUserName');
    
    // デフォルト画像パス
    const DEFAULT_PROFILE_IMAGE = '/images/default-profile.png';
    
    // ローカルストレージからユーザー情報取得または初期化
    initUserData();
    
    // ユーザーリストを取得して表示
    fetchUserList();
    
    function initUserData() {
        const storedUserId = localStorage.getItem('currentUserId');
        const storedUserName = localStorage.getItem('currentUserName');
        const storedUserImage = localStorage.getItem('currentUserImage');
        
        if (storedUserId && storedUserName) {
            currentUserName.textContent = storedUserName;
            
            if (storedUserImage) {
                currentUserImage.src = storedUserImage;
            } else {
                currentUserImage.src = DEFAULT_PROFILE_IMAGE;
            }
            
            // セッションにユーザーIDを設定
            setSessionUserId(storedUserId);
        } else {
            // デフォルト: ゲストユーザー
            currentUserName.textContent = 'ゲスト';
            currentUserImage.src = DEFAULT_PROFILE_IMAGE;
            
            // ローカルストレージに保存
            localStorage.setItem('currentUserId', '1'); // ゲストユーザーIDを1とする
            localStorage.setItem('currentUserName', 'ゲスト');
            localStorage.setItem('currentUserImage', DEFAULT_PROFILE_IMAGE);
            
            // セッションにユーザーIDを設定
            setSessionUserId('1');
        }
    }
    
    // サーバーからユーザーリストを取得
    async function fetchUserList() {
        try {
            const response = await fetch('/users/api/list');
            if (!response.ok) {
                console.error('ユーザーリスト取得エラー', response.statusText);
                return;
            }
            
            const users = await response.json();
            populateUserDropdown(users);
        } catch (error) {
            console.error('ユーザーリスト取得エラー:', error);
        }
    }
    
    // ドロップダウンメニューにユーザーリストを表示
    function populateUserDropdown(users) {
        // 既存の項目をクリア (最後の新規ユーザー作成以外)
        while (userDropdownMenu.children.length > 2) {
            userDropdownMenu.removeChild(userDropdownMenu.firstChild);
        }
        
        // ユーザーごとにメニュー項目を作成
        users.forEach(user => {
            const menuItem = document.createElement('li');
            const link = document.createElement('a');
            link.className = 'dropdown-item d-flex align-items-center';
            link.href = '#';
            link.dataset.userId = user.id;
            
            // プロフィール画像
            const img = document.createElement('img');
            img.src = user.profileImagePath || DEFAULT_PROFILE_IMAGE;
            img.alt = user.username;
            img.width = 24;
            img.height = 24;
            img.className = 'rounded-circle me-2';
            
            // ユーザー名
            const nameSpan = document.createElement('span');
            nameSpan.textContent = user.username;
            
            // 要素を組み立て
            link.appendChild(img);
            link.appendChild(nameSpan);
            menuItem.appendChild(link);
            
            // クリックイベント
            link.addEventListener('click', function(e) {
                e.preventDefault();
                switchUser(user.id, user.username, user.profileImagePath);
            });
            
            // 先頭に追加 (区切り線の前)
            userDropdownMenu.insertBefore(menuItem, userDropdownMenu.children[0]);
        });
    }
    
    // ユーザー切り替え
    async function switchUser(userId, username, profileImagePath) {
        try {
            // サーバーサイドでユーザー切り替え
            const response = await fetch(`/users/switch/${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                console.error('ユーザー切り替えエラー');
                return;
            }
            
            // UIを更新
            currentUserName.textContent = username;
            currentUserImage.src = profileImagePath || DEFAULT_PROFILE_IMAGE;
            
            // ローカルストレージに保存
            localStorage.setItem('currentUserId', userId);
            localStorage.setItem('currentUserName', username);
            localStorage.setItem('currentUserImage', profileImagePath || DEFAULT_PROFILE_IMAGE);
            
            // ページをリロード
            window.location.reload();
        } catch (error) {
            console.error('ユーザー切り替えエラー:', error);
        }
    }
    
    // セッションにユーザーIDを設定
    async function setSessionUserId(userId) {
        try {
            await fetch(`/users/switch/${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        } catch (error) {
            console.error('セッション更新エラー:', error);
        }
    }
});