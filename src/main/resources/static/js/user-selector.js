document.addEventListener('DOMContentLoaded', function() {
    // DOM要素
    const userDropdownMenu = document.getElementById('userDropdownMenu');
    const currentUserImage = document.getElementById('currentUserImage');
    const currentUserName = document.getElementById('currentUserName');
    const userProfileLink = document.querySelector('.user-selector a[href="/users/profile"]');
    
    // 初期化と読み込み（画像更新以外の機能を残す）
    initUserData();
    fetchUserList();
    
    // ユーザー情報の初期化と読み込み
    function initUserData() {
        const storedUserId = localStorage.getItem('currentUserId');
        const storedUserName = localStorage.getItem('currentUserName');
        
        if (storedUserId && storedUserName) {
            currentUserName.textContent = storedUserName;
            
            // ユーザープロフィールへのリンク先を設定
            if (userProfileLink) {
                userProfileLink.href = `/users/profile/${storedUserId}`;
            }
        } else {
            // デフォルト: ゲストユーザー
            currentUserName.textContent = 'ゲスト';
            
            localStorage.setItem('currentUserId', '1');
            localStorage.setItem('currentUserName', 'ゲスト');
            
            if (userProfileLink) {
                userProfileLink.href = '/users/profile/1';
            }
        }
    }
    
    // サーバーからユーザーリストを取得
    async function fetchUserList() {
        try {
            console.log('Fetching user list...');
            const response = await fetch('/users/api/list');
            if (!response.ok) {
                console.error('ユーザーリスト取得エラー', response.statusText);
                return;
            }
            
            const users = await response.json();
            console.log('User list received:', users);
            populateUserDropdown(users);
        } catch (error) {
            console.error('ユーザーリスト取得エラー:', error);
        }
    }
    
    function populateUserDropdown(users) {
        console.log('Populating dropdown with users:', users.length);
        
        // 既存の項目をクリア (すべて削除)
        while (userDropdownMenu.children.length > 0) {
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
            img.src = user.profileImagePath || '/images/default-profile.jpg';
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
            
            // メニューに追加
            userDropdownMenu.appendChild(menuItem);
        });
        
        // 区切り線
        const divider = document.createElement('li');
        divider.innerHTML = '<hr class="dropdown-divider">';
        userDropdownMenu.appendChild(divider);
        
        // 新規ユーザー作成リンク
        const newUserItem = document.createElement('li');
        newUserItem.innerHTML = '<a class="dropdown-item" href="/users/register"><i class="fas fa-user-plus me-2"></i>新規ユーザー作成</a>';
        userDropdownMenu.appendChild(newUserItem);
    }
    
    // ユーザー切り替え
    async function switchUser(userId, username, profileImagePath) {
        try {
            console.log('Switching to user:', username, userId);
            
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
            
            // ローカルストレージに保存
            localStorage.setItem('currentUserId', userId);
            localStorage.setItem('currentUserName', username);
            
            window.location.href = "/books";
        } catch (error) {
            console.error('ユーザー切り替えエラー:', error);
        }
    }
});