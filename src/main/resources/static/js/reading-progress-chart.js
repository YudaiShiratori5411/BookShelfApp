document.addEventListener('DOMContentLoaded', () => {
   // グローバルスコープのsessionsを使用
   if (typeof sessions === 'undefined') {
       console.error('セッションデータが見つかりません');
       return;  
   }

   // グローバル変数としてチャートインスタンスを保持
   let dailyChart = null;
   let cumulativeChart = null;

   // データを期間ごとに集計する関数
   const aggregateData = (sessions, period) => {
       const data = {};
       
       sessions.forEach(session => {
           // タイムゾーンを考慮した日付処理
           const date = new Date(session.startTime); 
           const localDate = new Date(date.getTime() - (date.getTimezoneOffset() * 60000));
           let key;
           
           switch(period) {
               case 'daily':
                   key = localDate.toISOString().split('T')[0];
                   break;
               case 'weekly':
                   const weekStart = new Date(localDate);
                   weekStart.setDate(localDate.getDate() - localDate.getDay());
                   key = weekStart.toISOString().split('T')[0];
                   break;
               case 'monthly':
                   key = `${localDate.getFullYear()}-${String(localDate.getMonth() + 1).padStart(2, '0')}`;
                   break;
               case 'yearly':
                   key = `${localDate.getFullYear()}`;
                   break;
           }
           
           data[key] = (data[key] || 0) + session.pagesRead;
       });

       return data;
   };

   // 日別の読書ページ数グラフ
   const drawDailyProgressChart = (sessions, period) => {
       const dailyData = aggregateData(sessions, period);
       
       const ctx = document.getElementById('dailyProgressChart');
       if (!ctx) {
           console.error('日別グラフ用のcanvas要素が見つかりません');
           return;
       }

       // 既存のチャートがあれば破棄
       if (dailyChart) {
           dailyChart.destroy();
       }

       dailyChart = new Chart(ctx, {
           type: 'bar',
           data: {
               labels: Object.keys(dailyData),
               datasets: [{
                   label: '読書ページ数',
                   data: Object.values(dailyData),
                   backgroundColor: 'rgba(54, 162, 235, 0.2)',
                   borderColor: 'rgba(54, 162, 235, 1)',
                   borderWidth: 1
               }]
           },
           options: {
               responsive: true,
               scales: {
                   x: {
                       offset: true,
                       ticks: {
                           padding: 5,
                           labelOffset: 20,
                           align: 'center',
                           rotation: 50,
                           minRotation: 50,
                           maxRotation: 50
                       },
                       grid: {
                           offset: true
                       }
                   },
                   y: {
                       beginAtZero: true,
                       title: {
                           display: true,
                           text: 'ページ数'
                       }
                   }
               },
               plugins: {
                   title: {
                       display: true,
                       text: '読書ページ数'
                   }
               },
               layout: {
                   padding: {
                       left: 10,
                       right: 10
                   }
               }
           }
       });
   };

   // 累積読書ページ数グラフ
   const drawCumulativeProgressChart = (sessions, period) => {
       let cumulative = 0;
       const cumulativeData = Object.entries(aggregateData(sessions, period))
           .sort(([a], [b]) => a.localeCompare(b))
           .map(([date, pages]) => {
               cumulative += pages;
               return { date, total: cumulative };
           });

       const ctx = document.getElementById('cumulativeProgressChart');
       if (!ctx) {
           console.error('累積グラフ用のcanvas要素が見つかりません');
           return;
       }

       // 既存のチャートがあれば破棄
       if (cumulativeChart) {
           cumulativeChart.destroy();
       }

       cumulativeChart = new Chart(ctx, {
           type: 'line',
           data: {
               labels: cumulativeData.map(d => d.date),
               datasets: [{
                   label: '累積読書ページ数',
                   data: cumulativeData.map(d => d.total),
                   borderColor: 'rgb(75, 192, 192)',
                   tension: 0.1,
                   fill: false
               }]
           },
           options: {
               responsive: true,
               scales: {
                   x: {
                       offset: true,
                       ticks: {
                           padding: 5,
                           labelOffset: 20,
                           align: 'center',
                           rotation: 50,
                           minRotation: 50,
                           maxRotation: 50
                       },
                       grid: {
                           offset: true
                       }
                   },
                   y: {
                       beginAtZero: true,
                       title: {
                           display: true,
                           text: '累積ページ数'
                       }
                   }
               },
               plugins: {
                   title: {
                       display: true,
                       text: '累積読書ページ数の推移'
                   }
               },
               layout: {
                   padding: {
                       left: 10,
                       right: 10
                   }
               }
           }
       });
   };

   // 期間選択ボタンのイベント処理
   const periodButtons = document.querySelectorAll('[data-period]');
   
   periodButtons.forEach(button => {
       button.addEventListener('click', function() {
           // アクティブなボタンのスタイルを更新
           periodButtons.forEach(btn => {
               btn.classList.remove('active');
               btn.classList.remove('btn-primary');
               btn.classList.add('btn-outline-primary');
           });
           this.classList.add('active');
           this.classList.remove('btn-outline-primary');
           this.classList.add('btn-primary');

           // 選択された期間でグラフを更新
           const period = this.dataset.period;
           drawDailyProgressChart(sessions, period);
           drawCumulativeProgressChart(sessions, period);
       });
   });

   // 初期表示
   drawDailyProgressChart(sessions, 'daily');
   drawCumulativeProgressChart(sessions, 'daily');
});

