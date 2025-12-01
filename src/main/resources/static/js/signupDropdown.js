document.addEventListener("DOMContentLoaded", () => {

    /* ---------------------------
     * 공통 드롭다운 생성 함수
     * --------------------------- */
    function setupCustomDropdown(buttonId, menuId, selectedTextId, hiddenSelectId, iconId) {
        const btn = document.getElementById(buttonId);
        const menu = document.getElementById(menuId);
        const selectedText = document.getElementById(selectedTextId);
        const hiddenSelect = document.getElementById(hiddenSelectId);
        const icon = document.getElementById(iconId);

        if (!btn || !menu || !selectedText || !hiddenSelect || !icon) return;

        // 드롭다운 열고 닫기
        btn.addEventListener("click", () => {
            menu.classList.toggle("hidden");
            icon.classList.toggle("rotate-180");
        });

        // 옵션 클릭 시
        menu.querySelectorAll(".dropdown-option").forEach(option => {
            option.addEventListener("click", () => {
                const value = option.dataset.value;
                const label = option.innerText.trim();

                selectedText.innerText = label;
                hiddenSelect.value = value;

                menu.classList.add("hidden");
                icon.classList.remove("rotate-180");
            });
        });

        // 바깥 클릭 시 닫기
        document.addEventListener("click", (e) => {
            if (!btn.contains(e.target) && !menu.contains(e.target)) {
                menu.classList.add("hidden");
                icon.classList.remove("rotate-180");
            }
        });
    }

    /* festivalDataLoad.js에서 전역처리 - 하요한
     * ---------------------------
     * 지역 드롭다운 연결 (기존 동일)
     * ---------------------------

    setupCustomDropdown(
        "regionDropdownBtn",
        "regionDropdownMenu",
        "regionSelectedText",
        "regionFilter",
        "regionDropdownIcon"
    );*/


    /* ---------------------------
     * 성별 드롭다운 연결
     * --------------------------- */
    setupCustomDropdown(
        "genderDropdownBtn",
        "genderDropdownMenu",
        "genderSelectedText",
        "genderSelect",
        "genderDropdownIcon"
    );


    /* ---------------------------
     * 선호 태그 드롭다운 연결
     * --------------------------- */
    setupCustomDropdown(
        "tagDropdownBtn",
        "tagDropdownMenu",
        "tagSelectedText",
        "tagSelect",
        "tagDropdownIcon"
    );

});
