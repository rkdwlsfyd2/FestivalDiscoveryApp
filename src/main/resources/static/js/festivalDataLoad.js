// 해당 페이지 내용 : 로딩 오버레이 표시 및 제거,축제 목록 ajax 요청 및 마커 업데이트 함수
// 필터링 상태 감지도 여기서 함
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

// 축제 목록 데이터 비동기 요청
function updateFestivalList(page = 0,
                            keyword = "",
                            ongoingOnly = false,
                            updateMarkers = true,
                            region = "",
                            tag = "") {
    showLoadingOverlay();

    const keywordInput = document.querySelector("#keywordInput");
    const searchKeyword = keyword || (keywordInput?.value || "");

    const ongoingOnlyCheckbox = document.querySelector("#ongoingOnlyCheckbox");
    const isOngoingOnly = ongoingOnly || (ongoingOnlyCheckbox?.checked || false);

    const regionFilter = document.querySelector("#regionFilter");
    const selectedRegion = region || (regionFilter?.value || "");

    const selectedTag = tag || (window.selectedTag || "");

    const isAdmin = window.isAdmin === true;

    let url = `/festivals/festivalMap/ajax?page=${page}` +
        `&keyword=${encodeURIComponent(searchKeyword)}` +
        `&ongoingOnly=${isOngoingOnly}`;
    if (selectedRegion) {
        url += `&region=${encodeURIComponent(selectedRegion)}`;
    }
    if (selectedTag) {
        url += `&tag=${encodeURIComponent(selectedTag)}`;
    }
    url += `&isAdmin=${encodeURIComponent(isAdmin)}`;
    url += `&_=${new Date().getTime()}`;

    fetch(url)
        .then(res => res.text())
        .then(html => {
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, "text/html");

            const newRoot = doc.querySelector("#festivalListRoot");
            const oldRoot = document.querySelector("#festivalListRoot");

            if (newRoot && oldRoot) {
                oldRoot.replaceWith(newRoot);
            } else {
                console.warn("festivalListRoot를 찾을 수 없습니다.", { newRoot, oldRoot });
            }

            if (updateMarkers && typeof window.updateKakaoMarkersFromDom === "function") {
                window.updateKakaoMarkersFromDom({ keepBounds: true });
            }
        })
        .finally(() => {
            removeLoadingOverlay();
        });
}

// 전역 플래그: 리스너 중복 등록 방지
if (!window.festivalFilterInitialized) {
    window.festivalFilterInitialized = true;

    // 페이지네이션 클릭
    document.addEventListener("click", function (e) {
        const btn = e.target.closest("#pagination .page-btn");
        if (!btn) return;

        e.preventDefault();

        const page = parseInt(btn.dataset.page, 10) || 0;
        updateFestivalList(page);

        window.scrollTo({ top: 0, behavior: "smooth" });
    });

    // "현재 진행중인 축제만 보기" 체크박스
    document.addEventListener("change", function (e) {
        if (e.target && e.target.id === "ongoingOnlyCheckbox") {
            updateFestivalList(0);
        }
    });
}


// ==============================
//  공통 별 UI 적용 함수
// ==============================
function applyStarUI(button, result) {
    if (!button) return;

    if (result === "added") {
        button.textContent = "★";
        button.classList.remove("bg-white", "border-gray-300", "text-gray-700");
        button.classList.add("bg-yellow-300", "border-yellow-400", "text-gray-900");
    } else {
        button.textContent = "☆";
        button.classList.add("bg-white", "border-gray-300", "text-gray-700");
        button.classList.remove("bg-yellow-300", "border-yellow-400", "text-gray-900");
    }
}


// ==============================
//  리스트 내 동일 축제 버튼 동기화
// ==============================
function syncListButtons(festivalNo, result) {
    document
        .querySelectorAll(`.fd-fav-form input[name="festivalNo"][value="${festivalNo}"]`)
        .forEach(input => {
            const btn = input.closest("form")?.querySelector("button");
            applyStarUI(btn, result);
        });
}


// ==============================
//  지도 오버레이 버튼 동기화
// ==============================
function syncOverlayButton(festivalNo, result) {
    const overlayBtn = document.querySelector(`.fd-overlay-fav[data-festival-no="${festivalNo}"]`);
    if (overlayBtn) {
        applyStarUI(overlayBtn, result);
    }
}


// ==============================
//  캘린더 openDate 유지 새로고침
// ==============================
function refreshCalendarWithOpenDate() {
    const params = new URLSearchParams(location.search);
    let openDate = params.get("openDate");

    // URL 에 openDate 있는 경우 → 그대로 유지
    if (openDate) {
        location.href = `${location.pathname}?${params.toString()}`;
        return;
    }

    // 선택된 날짜 셀에서 가져오기
    const selectedCell = document.querySelector(".calendar-day.bg-sub-blue.bg-opacity-30");
    if (selectedCell) {
        openDate = selectedCell.dataset.date;
        params.set("openDate", openDate);
        location.href = `${location.pathname}?${params.toString()}`;
        return;
    }

    // 없으면 오늘 날짜 선택
    const todayCell = document.querySelector('.calendar-day[data-istoday="true"]');
    if (todayCell) {
        openDate = todayCell.dataset.date;
        params.set("openDate", openDate);
        location.href = `${location.pathname}?${params.toString()}`;
        return;
    }

    // fallback
    location.href = `${location.pathname}?${params.toString()}`;
}


// ==============================
//  최종 완성형 즐겨찾기 토글 함수
// ==============================
async function toggleFav(btn) {

    if (!window.loginUser) {
        location.href = "/login";
        return;
    }

    const form = btn.closest("form");
    const festivalNo = form.festivalNo.value;

    const res = await fetch("/favorite/toggle", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `festivalNo=${festivalNo}`
    });

    const result = await res.text();

    // ------------------------
    // 1) 리스트의 버튼 UI 토글
    // ------------------------
    if (result === "added") {
        btn.textContent = "★";
        btn.classList.remove("bg-white", "border-gray-300", "text-gray-700");
        btn.classList.add("bg-yellow-300", "border-yellow-400", "text-gray-900");
    } else {
        btn.textContent = "☆";
        btn.classList.add("bg-white", "border-gray-300", "text-gray-700");
        btn.classList.remove("bg-yellow-300", "border-yellow-400", "text-gray-900");
    }

    // ------------------------
    // 2) 동일 festivalNo 모든 리스트 버튼 동기화
    // ------------------------
    document.querySelectorAll(`.fd-fav-form input[value="${festivalNo}"]`)
        .forEach(input => {
            const btn2 = input.closest("form").querySelector("button");
            if (!btn2) return;
            if (result === "added") {
                btn2.textContent = "★";
                btn2.classList.remove("bg-white", "border-gray-300", "text-gray-700");
                btn2.classList.add("bg-yellow-300", "border-yellow-400", "text-gray-900");
            } else {
                btn2.textContent = "☆";
                btn2.classList.add("bg-white", "border-gray-300", "text-gray-700");
                btn2.classList.remove("bg-yellow-300", "border-yellow-400", "text-gray-900");
            }
        });

    // ------------------------
    // 3) 지도 오버레이 즐찾 아이콘도 즉시 반영
    // ------------------------
    const overlayBtn = document.querySelector(`.fd-overlay-fav[data-festival-no="${festivalNo}"]`);

    if (overlayBtn) {
        if (result === "added") {
            overlayBtn.textContent = "★";
            overlayBtn.classList.add("bg-yellow-300", "border-yellow-400", "text-gray-900");
            overlayBtn.classList.remove("bg-white", "border-gray-300", "text-gray-700");
        } else {
            overlayBtn.textContent = "☆";
            overlayBtn.classList.add("bg-white", "border-gray-300", "text-gray-700");
            overlayBtn.classList.remove("bg-yellow-300", "border-yellow-400", "text-gray-900");
        }
    }

    // ------------------------
    // 4) 지도 마커 + 오버레이 전체 재동기화 (중요!!)
    // ------------------------
    if (window.updateKakaoMarkersFromDom) {
        window.updateKakaoMarkersFromDom({ keepBounds: true });
    }

    // ------------------------
    // 5) 캘린더에서 즐겨찾기 눌렀을 때 openDate 유지
    // ------------------------
    if (location.pathname.startsWith("/calendar")) {

        const params = new URLSearchParams(location.search);
        let openDate = params.get("openDate");

        if (openDate) {
            params.set("openDate", openDate);
            location.href = `${location.pathname}?${params.toString()}`;
            return;
        }

        const autoCell = document.querySelector(".calendar-day.bg-sub-blue.bg-opacity-30");
        if (autoCell) {
            params.set("openDate", autoCell.dataset.date);
            location.href = `${location.pathname}?${params.toString()}`;
            return;
        }

        const todayCell = document.querySelector(".calendar-day[data-istoday='true']");
        if (todayCell) {
            params.set("openDate", todayCell.dataset.date);
            location.href = `${location.pathname}?${params.toString()}`;
            return;
        }

        location.href = `${location.pathname}?${params.toString()}`;
        return;
    }
}




/*
// 안쓰는 함수 -하요한- 네이버맵 마커 업데이트 함수(여기서 마커 찍어줌)
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
*/

