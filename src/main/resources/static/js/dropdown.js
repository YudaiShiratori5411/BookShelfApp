// モダンなドロップダウンメニューの動作
document.addEventListener('DOMContentLoaded', function() {
    const dropdowns = document.querySelectorAll('.modern-dropdown');

    // ドロップダウンの開閉処理
    dropdowns.forEach(dropdown => {
        const toggle = dropdown.querySelector('.modern-dropdown-toggle');
        const menu = dropdown.querySelector('.modern-dropdown-menu');

        toggle.addEventListener('click', (e) => {
            e.stopPropagation();
            const isOpen = menu.classList.contains('show');
            
            // 他のすべてのドロップダウンを閉じる
            document.querySelectorAll('.modern-dropdown-menu.show').forEach(openMenu => {
                if (openMenu !== menu) {
                    openMenu.classList.remove('show');
                }
            });

            // クリックされたドロップダウンの切り替え
            menu.classList.toggle('show');
            toggle.setAttribute('aria-expanded', !isOpen);
        });

        // ドロップダウンメニュー内のクリックイベントの伝播を停止
        menu.addEventListener('click', (e) => {
            e.stopPropagation();
        });
    });

    // ドロップダウン以外の場所をクリックした時に閉じる
    document.addEventListener('click', () => {
        document.querySelectorAll('.modern-dropdown-menu.show').forEach(menu => {
            menu.classList.remove('show');
            const toggle = menu.previousElementSibling;
            if (toggle) {
                toggle.setAttribute('aria-expanded', 'false');
            }
        });
    });

    // キーボード操作のサポート
    dropdowns.forEach(dropdown => {
        const menu = dropdown.querySelector('.modern-dropdown-menu');
        const items = menu.querySelectorAll('.modern-dropdown-item');

        items.forEach((item, index) => {
            item.addEventListener('keydown', (e) => {
                switch (e.key) {
                    case 'ArrowDown':
                        e.preventDefault();
                        if (index < items.length - 1) {
                            items[index + 1].focus();
                        }
                        break;
                    case 'ArrowUp':
                        e.preventDefault();
                        if (index > 0) {
                            items[index - 1].focus();
                        }
                        break;
                    case 'Escape':
                        e.preventDefault();
                        menu.classList.remove('show');
                        dropdown.querySelector('.modern-dropdown-toggle').focus();
                        break;
                }
            });
        });
    });
});