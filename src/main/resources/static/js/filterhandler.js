// 페이지네이션,축제명 검색,태그 버튼,지역별 드롭다운 UI 조작 및 
// change 이벤트 처리 및 상태 감지
if (!window.filterHandlerInitialized) {
    window.filterHandlerInitialized = true;
    console.log("[필터 핸들러] 초기화 시작");
    
    document.addEventListener("DOMContentLoaded", function() {
        console.log("[필터 핸들러] DOMContentLoaded 이벤트 발생");
        
        // ========== 페이지네이션 ==========
        document.addEventListener("click", function (e) {
            // 페이지네이션 버튼을 눌렀을 때
            if (e.target.classList.contains("page-btn")) {
                e.preventDefault();
                const page = e.target.dataset.page;
                if (typeof updateFestivalList === 'function') {
                    updateFestivalList(page);
                }
            }
            // 검색 버튼을 눌렀을 때 
            if (e.target.id === "searchBtn" || e.target.closest("#searchBtn")) {
                e.preventDefault();
                if (typeof updateFestivalList === 'function') {
                    updateFestivalList(0);
                }
            }
        });
        
        // ========== 축제명 검색 ==========
        // 검색창에서 Enter 키를 눌렀을 때
        const keywordInput = document.querySelector("#keywordInput");
        if (keywordInput) {
            keywordInput.addEventListener("keypress", function (e) {
                if (e.key === "Enter") {
                    e.preventDefault();
                    if (typeof updateFestivalList === 'function') {
                        updateFestivalList(0);
                    }
                }
            });
        }
        
        // ========== 현재 진행중인 축제만 보기 체크박스 ==========
        const ongoingOnlyCheckbox = document.querySelector("#ongoingOnlyCheckbox");
        if (ongoingOnlyCheckbox) {
            ongoingOnlyCheckbox.addEventListener("change", function(e) {
                const isChecked = e.target.checked;
                // 체크박스 상태 변경 시 첫 페이지로 리셋하여 목록 업데이트
                if (typeof updateFestivalList === 'function') {
                    updateFestivalList(0, "", isChecked);
                }
            });
        }
        
        // ========== 태그 필터 버튼 ==========
        const tagButtons = document.querySelectorAll(".tag-btn");
        tagButtons.forEach(function(btn) {
            btn.addEventListener("click", function(e) {
                e.preventDefault();
                
                // 클릭한 버튼이 이미 선택된 상태인지 확인
                const isSelected = btn.style.background && btn.style.background.includes("gradient");
                
                if (isSelected) {
                    // 선택된 상태면 선택 해제
                    btn.style.background = "";
                    window.selectedTag = ""; // 태그 선택 해제
                } else {
                    // 선택되지 않은 상태면 다른 버튼 선택 해제 후 클릭한 버튼 선택
                    tagButtons.forEach(function(button) {
                        button.style.background = ""; // 기본 배경색 복원
                    });
                    
                    // 클릭한 버튼에 선택 상태 추가 (bg-gradient2 그라디언트 배경)
                    btn.style.background = "linear-gradient(45deg, #5170ff, #ff66c4)";
                    
                    // 선택된 태그 저장
                    const selectedTag = btn.getAttribute("data-tag");
                    window.selectedTag = selectedTag || "";
                }
                
                // 축제 목록 업데이트
                if (typeof updateFestivalList === 'function') {
                    updateFestivalList(0, "", false, true, "", window.selectedTag);
                }
                
            });
        });
        
        // ========== 지역별 드롭다운 UI 조작 ==========
        const regionDropdownBtn = document.querySelector("#regionDropdownBtn");
        const regionDropdownMenu = document.querySelector("#regionDropdownMenu");
        const regionSelectedText = document.querySelector("#regionSelectedText");
        const regionFilter = document.querySelector("#regionFilter");
        const regionOptions = document.querySelectorAll(".region-option");
        
        if (regionDropdownBtn && regionDropdownMenu) {
            // 이미 등록된 리스너가 있으면 제거
            if (regionDropdownBtn._clickHandler) {
                regionDropdownBtn.removeEventListener("click", regionDropdownBtn._clickHandler);
            }
            
            // 중복 클릭 방지를 위한 플래그
            let isProcessing = false;
            
            // 드롭다운 버튼 클릭 시 메뉴 토글
            const clickHandler = function(e) {
                // 이미 처리 중이면 무시
                if (isProcessing) {
                    console.log("[드롭다운 클릭] 이미 처리 중이므로 무시");
                    return;
                }
                
                e.preventDefault();
                e.stopPropagation();
                isProcessing = true;
                
                window.lastRegionButtonClickTime = Date.now();
                window.regionButtonJustClicked = true;
                
                // 외부 클릭 핸들러가 무시하도록 짧은 딜레이
                setTimeout(function() {
                    window.regionButtonJustClicked = false;
                }, 300);
                
                const isHidden = regionDropdownMenu.classList.contains("hidden");
                
                if (isHidden) {
                    console.log("[드롭다운 클릭] 메뉴 열기 시도");
                    regionDropdownMenu.classList.remove("hidden");
                } else {
                    regionDropdownMenu.classList.add("hidden");
                }
                
                setTimeout(function() {
                    isProcessing = false;
                    console.log("[드롭다운 클릭] 처리 완료, 플래그 해제");
                }, 100);
            };
            
            // 리스너 등록 및 참조 저장
            regionDropdownBtn.addEventListener("click", clickHandler);
            regionDropdownBtn._clickHandler = clickHandler;
            
            // 옵션 선택 시
            regionOptions.forEach(function(option) {
                option.addEventListener("click", function(e) {
                    e.stopPropagation();
                    const value = option.getAttribute("data-value");
                    const text = option.textContent.trim();
                    
                    if (regionSelectedText) {
                        regionSelectedText.textContent = text;
                    }
                    
                    if (regionFilter) {
                        regionFilter.value = value;
                        regionFilter.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                    
                    regionDropdownMenu.classList.add("hidden");
                });
            });
            
            // 지역 필터 change 이벤트 리스너
            if (regionFilter) {
                regionFilter.addEventListener("change", function(e) {
                    const selectedRegion = e.target.value || "";
                    if (typeof updateFestivalList === 'function') {
                        updateFestivalList(0, "", false, true, selectedRegion);
                    }
                });
            }
        }
    });
}

// 외부 클릭 시 드롭다운 닫기
if (!window.regionDropdownClickHandlerRegistered) {
    window.regionDropdownClickHandlerRegistered = true;
    window.lastRegionButtonClickTime = 0;
    window.regionButtonJustClicked = false;

    document.addEventListener("click", function(e) {
        const regionDropdownBtn = document.querySelector("#regionDropdownBtn");
        const regionDropdownMenu = document.querySelector("#regionDropdownMenu");
        
        if (regionDropdownBtn && regionDropdownMenu) {
            const isButtonOrMenu = regionDropdownBtn.contains(e.target) || regionDropdownMenu.contains(e.target);
            const timeSinceButtonClick = Date.now() - (window.lastRegionButtonClickTime || 0);
            const buttonJustClicked = window.regionButtonJustClicked || timeSinceButtonClick < 300;
            
            // 버튼이나 메뉴를 클릭한 경우 무조건 무시
            if (isButtonOrMenu) {
                console.log("[외부 클릭] 버튼/메뉴 클릭이므로 무시");
                return;
            }
            
            // 버튼 클릭 직후인 경우 무시
            if (buttonJustClicked) {
                console.log("[외부 클릭] 버튼 클릭 직후라서 무시");
                return;
            }
            
            // 외부 클릭인 경우에만 닫기
            regionDropdownMenu.classList.add("hidden");
        }
    });
}

