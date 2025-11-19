// 해당 페이지 내용 : 로딩 오버레이 표시 및 제거,축제 목록 ajax 요청 및 마커 업데이트 함수 
// "로딩 오버레이" 표시
function showLoadingOverlay() {
    // 이미 존재하면 중복 생성 X
    if (document.getElementById("loadingOverlay")) return;
    const overlay = document.createElement("div");
    overlay.id = "loadingOverlay";
    overlay.className = "fixed inset-0 bg-white/70 backdrop-blur-sm flex flex-col items-center justify-center z-50";
    overlay.innerHTML = `
        <div class="loading-spinner"></div>
        <div class="loading-text mt-2 text-gray-700">불러오는 중...</div>
    `;
    document.body.appendChild(overlay);
}

function removeLoadingOverlay() {
    const overlay = document.getElementById("loadingOverlay");
    if (overlay) overlay.remove();
}

//축제 목록 데이터 비동기 요청
function updateFestivalList(page = 0, keyword = "", ongoingOnly = false, updateMarkers = true) {
    showLoadingOverlay();

    // 검색어 감지(null일때 처리)
    const keywordInput = document.querySelector("#keywordInput");
    const searchKeyword = keyword || (keywordInput?.value || "");
    
    // 체크박스 상태 감지
    const ongoingOnlyCheckbox = document.querySelector("#ongoingOnlyCheckbox");
    const isOngoingOnly = ongoingOnly || (ongoingOnlyCheckbox?.checked || false);
    
    // .festival-items인 요소에 데이터를 넣어준다.
    const items = document.querySelector(".festival-items");

    const url = `/festivals/review/ajax?page=${page}&keyword=${encodeURIComponent(searchKeyword)}&ongoingOnly=${isOngoingOnly}&_=${new Date().getTime()}`;
    fetch(url)
        .then(res => res.text())
        .then(html => {
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, "text/html");

            const newItems = doc.querySelector(".festival-items");
            const newPagination = doc.querySelector("#pagination");

            const pagination = document.querySelector("#pagination");

            if (items && newItems) items.replaceChildren(...newItems.children);
            if (pagination && newPagination) pagination.replaceChildren(...newPagination.children);
            
            // 마커 업데이트는 검색/필터링 시에만 수행 (페이지네이션 제외)
            if (updateMarkers) {
                // script 태그를 실행하여 축제 JSON 데이터 javascript 변수 설정
                const jsonScripts = doc.querySelectorAll('script');
                jsonScripts.forEach(function(script) {
                    if (script.textContent && script.textContent.includes('ajaxFestivalsJson')) {
                        try {
                            // script 내용 실행
                            eval(script.textContent);
                        } catch (e) {
                            console.warn('[AJAX] Script 실행 중 오류:', e);
                        }
                    }
                });
                
                // window.ajaxFestivalsJson(축제 JSON 데이터) 데이터 가져와서 파싱
                if (typeof window.ajaxFestivalsJson !== 'undefined') {
                    try {
                        const festivalsJson = typeof window.ajaxFestivalsJson === 'string' 
                            ? JSON.parse(window.ajaxFestivalsJson) 
                            : window.ajaxFestivalsJson;
                        updateMapMarkers(festivalsJson);
                    } catch (e) {
                        console.error('[AJAX] JSON 파싱 실패:', e);
                    }
                } else {
                    console.warn('[AJAX] ajaxFestivalsJson을 찾을 수 없습니다.');
                }
            }
        })
        .finally(() => {
            removeLoadingOverlay(); // 요청 완료 시 오버레이 제거
        });
}

// 네이버맵 마커 업데이트 함수(여기서 마커 찍어줌)
function updateMapMarkers(festivalsJson) {
    // 네이버맵 SDK 초기화, 지도 인스턴스, maps 객체 확인
    if (!window.naverMapInstance || typeof naver === 'undefined' || !naver.maps) {
        console.warn('[마커 업데이트] 네이버맵이 초기화되지 않았습니다.');
        return;
    }
    
    const map = window.naverMapInstance;
    
    // 기존(검색 전) 마커와 정보창 제거 함수
    if (window.currentMarkers) {
        window.currentMarkers.forEach(function(marker) {
            marker.setMap(null);
        });
    }
    if (window.currentInfoWindows) {
        window.currentInfoWindows.forEach(function(infowindow) {
            infowindow.close();
        });
    }
    
    // 축제 데이터 변환(JSON 데이터 -> 마커 데이터)
    const festivalMarkers = festivalsJson.map(function(festival) {
        return {
            festivalNo: festival.festivalNo,
            name: festival.title || '축제명 없음',
            address: festival.addr || '주소 없음',
            lat: festival.mapy || null,
            lng: festival.mapx || null,
            startDate: festival.eventStartDate || null,
            endDate: festival.eventEndDate || null
        };
    }).filter(function(festival) {
        return festival.lat !== null && festival.lng !== null && 
               !isNaN(festival.lat) && !isNaN(festival.lng);
    });
    
    // 새 마커, 정보창(실제로는 infoContent.html에서) 생성
    const markers = [];
    const infoWindows = [];
    
    // 마커 한번에 뿌려주기
    festivalMarkers.forEach(function(festival, index) {
        try {
            const position = new naver.maps.LatLng(festival.lat, festival.lng);
            const marker = new naver.maps.Marker({
                position: position,
                map: map,
                shadow: false
            });
            
            // 정보창 생성 및 이벤트 리스너 연결 (infoContent.html의 함수 사용)
            const infowindow = createInfoWindow(festival, marker, map, infoWindows);
            
            markers.push(marker);
            infoWindows.push(infowindow);
        } catch (e) {
            console.error(`[마커 업데이트] 마커 생성 실패 (인덱스 ${index}):`, e);
        }
    });
    
    // 전역 변수에 저장
    window.currentMarkers = markers;
    window.currentInfoWindows = infoWindows;
    
    // 검색 시 지도 범위를 남한 중심으로 리셋
    const center = new naver.maps.LatLng(36.0, 127.5); // 남한 중심 (대전 근처)
    map.setCenter(center);
    map.setZoom(7); 
    
    console.log('[마커 업데이트] 완료 - 마커(축제) 개수:', markers.length);
}

