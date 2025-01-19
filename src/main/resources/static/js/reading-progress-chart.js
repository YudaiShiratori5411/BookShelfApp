document.addEventListener('DOMContentLoaded', () => {
   if (typeof sessions === 'undefined') {
       console.error('セッションデータが見つかりません');
       return;  
   }

   let dailyChart = null;
   let cumulativeChart = null;

   const aggregateData = (sessions, period) => {
       const data = {};
       
       sessions.forEach(session => {
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

   const drawDailyProgressChart = (sessions, period) => {
       const dailyData = aggregateData(sessions, period);
       const maxValue = Math.max(...Object.values(dailyData), 0);
       
       const ctx = document.getElementById('dailyProgressChart');
       if (!ctx) {
           console.error('日別グラフ用のcanvas要素が見つかりません');
           return;
       }

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
               maintainAspectRatio: false,
               aspectRatio: 1,
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
                       suggestedMax: maxValue + 10,
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
                       right: 10,
                       top: 20,
                       bottom: 20
                   }
               }
           }
       });
   };

   const drawCumulativeProgressChart = (sessions, period) => {
       let cumulative = 0;
       const cumulativeData = Object.entries(aggregateData(sessions, period))
           .sort(([a], [b]) => a.localeCompare(b))
           .map(([date, pages]) => {
               cumulative += pages;
               return { date, total: cumulative };
           });

       const maxCumulativeValue = Math.max(...cumulativeData.map(d => d.total), 0);

       const ctx = document.getElementById('cumulativeProgressChart');
       if (!ctx) {
           console.error('累積グラフ用のcanvas要素が見つかりません');
           return;
       }

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
               maintainAspectRatio: false,
               aspectRatio: 1,
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
                       suggestedMax: maxCumulativeValue + 50,
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
                       right: 10,
                       top: 20,
                       bottom: 20
                   }
               }
           }
       });
   };

   const periodButtons = document.querySelectorAll('[data-period]');
   
   periodButtons.forEach(button => {
       button.addEventListener('click', function() {
           periodButtons.forEach(btn => {
               btn.classList.remove('active');
               btn.classList.remove('btn-primary');
               btn.classList.add('btn-outline-primary');
           });
           this.classList.add('active');
           this.classList.remove('btn-outline-primary');
           this.classList.add('btn-primary');

           const period = this.dataset.period;
           drawDailyProgressChart(sessions, period);
           drawCumulativeProgressChart(sessions, period);
       });
   });

   drawDailyProgressChart(sessions, 'daily');
   drawCumulativeProgressChart(sessions, 'daily');
});

