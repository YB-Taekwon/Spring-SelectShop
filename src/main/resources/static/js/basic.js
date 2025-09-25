const host = window.location.origin;
let targetId;
let folderTargetId; // 현재 선택(필터)된 폴더 ID (전체: null/undefined)


$(document).ready(function () {
    const auth = getToken();

    if (auth !== undefined && auth !== '') {
        $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
            jqXHR.setRequestHeader('Authorization', auth);
        });
    } else {
        window.location.href = '/api/user/login-page';
        return;
    }

    $.ajax({
        type: 'GET',
        url: `/api/user-info`,
        contentType: 'application/json',
    })
        .done(function (res, status, xhr) {
            const username = res.username;
            const isAdmin = !!res.admin;

            if (!username) {
                window.location.href = '/api/user/login-page';
                return;
            }

            $('#username').text(username);
            if (isAdmin) {
                $('#admin').text(true);
                showProduct();
                loadUserFolders();
            } else {
                showProduct();
            }
        })
        .fail(function (jqXHR, textStatus) {
            logout();
        });

    // id 가 query 인 요소 위에서 엔터를 누르면 execSearch() 함수를 실행
    $('#query').on('keypress', function (e) {
        if (e.key == 'Enter') {
            execSearch();
        }
    });
    $('#close').on('click', function () {
        $('#container').removeClass('active');
    })
    $('#close2').on('click', function () {
        $('#container2').removeClass('active');
    })
    $('.nav div.nav-see').on('click', function () {
        $('div.nav-see').addClass('active');
        $('div.nav-search').removeClass('active');

        $('#see-area').show();
        $('#search-area').hide();
    })
    $('.nav div.nav-search').on('click', function () {
        $('div.nav-see').removeClass('active');
        $('div.nav-search').addClass('active');

        $('#see-area').hide();
        $('#search-area').show();
    })

    $('#see-area').show();
    $('#search-area').hide();
})

// 폴더 버튼(개별) 클릭
$(document).on('click', 'button.product-folder', function () {
    const id = $(this).val();
    openFolder(id ? Number(id) : null);
});

// 전체보기 버튼 클릭
$(document).on('click', '#folder-all', function () {
    openFolder(null);
});

// 폴더 추가 모달 열기 버튼(있다면)
$(document).on('click', '#open-add-folder', function () {
    openAddFolderPopup();
});

// 폴더 입력 칸 추가 버튼(있다면)
$(document).on('click', '#add-folder-input', function () {
    addFolderInput();
});

// 새 폴더 생성 버튼(있다면)
$(document).on('click', '#submit-folders', function () {
    addFolder();
});

function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function execSearch() {
    /**
     * 검색어 input id: query
     * 검색결과 목록: #search-result-box
     * 검색결과 HTML 만드는 함수: addHTML
     */

        // 1. 검색창의 입력값을 가져옴
    let query = $('#query').val();

    // 2. 검색창 입력값을 검사하고, 입력하지 않았을 경우 focus
    if (query == '') {
        alert('검색어를 입력해주세요');
        $('#query').focus();
        return;
    }
    // 3. GET /api/search?query=${query} 요청
    $.ajax({
        type: 'GET',
        url: `/api/search?query=${query}`,
        success: function (response) {
            $('#search-result-box').empty();
            // 4. for 문마다 itemDto 기반의 HTML 코드 생성 후 검색 결과 목록에 붙여서 화면에 표시
            for (let i = 0; i < response.length; i++) {
                let itemDto = response[i];
                let tempHtml = addHTML(itemDto);
                $('#search-result-box').append(tempHtml);
            }
        },
        error(error, status, request) {
            logout();
        }
    })
}

function addHTML(itemDto) {
    /**
     * class="search-itemDto" 인 요소에서
     * image, title, lprice, addProduct 활용
     * 참고) onclick='addProduct(${JSON.stringify(itemDto)})'
     */

    return `<div class="search-itemDto">
        <div class="search-itemDto-left">
            <img src="${itemDto.image}" alt="">
        </div>
        <div class="search-itemDto-center">
            <div>${itemDto.title}</div>
            <div class="price">
                ${numberWithCommas(itemDto.lprice)}
                <span class="unit">원</span>
            </div>
        </div>
        <div class="search-itemDto-right">
            <img src="../images/icon-save.png" alt="" onclick='addProduct(${JSON.stringify(itemDto)})'>
        </div>
    </div>`
}

function addProduct(itemDto) {
    /**
     * modal 뜨게 하는 법: $('#container').addClass('active');
     * data를 ajax로 전달할 때는 두 가지가 매우 중요
     * 1. contentType: "application/json",
     * 2. data: JSON.stringify(itemDto),
     */

    // 1. POST /api/products 에 관심 상품 생성 요청
    $.ajax({
        type: 'POST',
        url: '/api/products',
        contentType: 'application/json',
        data: JSON.stringify(itemDto),
        success: function (response) {
            // 2. 응답 함수에서 modal을 뜨게 하고, targetId 를 reponse.id 로 설정
            $('#container').addClass('active');
            targetId = response.id;
        },
        error(error, status, request) {
            logout();
        }
    });
}

function showProduct() {
    /**
     * 관심상품 목록: #product-container
     * 검색결과 목록: #search-result-box
     * 관심상품 HTML 만드는 함수: addProductItem
     */

        // 정렬/오름차순 파라미터
    const sorting = $("#sorting option:selected").val();
    const isAsc = $(':radio[name="isAsc"]:checked').val();

    // 폴더 필터링 지원: folderTargetId 가 있으면 해당 폴더의 상품, 없으면 전체
    const dataSource = (folderTargetId !== undefined && folderTargetId !== null)
        ? `/api/folders/${folderTargetId}/products?sortBy=${sorting}&isAsc=${isAsc}`
        : `/api/products?sortBy=${sorting}&isAsc=${isAsc}`;

    $('#product-container').empty();
    $('#search-result-box').empty();

    $('#pagination').pagination({
        dataSource,
        locator: 'content',
        alias: {
            pageNumber: 'page',
            pageSize: 'size'
        },
        totalNumberLocator: (response) => response.totalElements,
        pageSize: 10,
        showPrevious: true,
        showNext: true,
        ajax: {
            error(error) {
                if (error.status === 403) {
                    $('html').html(error.responseText);
                    return;
                }
                logout();
            }
        },
        callback: function (response) {
            $('#product-container').empty();
            for (const product of response) {
                const tempHtml = addProductItem(product);
                $('#product-container').append(tempHtml);
            }
        }
    });
}

function addProductItem(product) {
    // product.productFolderList : [{id, name}] 형태를 가정
    const folders = (product.productFolderList || []).map(folder =>
        `<span class="product-tag" onclick="openFolder(${folder.id})">#${folder.name}</span>`
    ).join('');

    // 폴더 추가 버튼(선택 UI는 동적으로 삽입)
    const addBtn = `
        <span class="product-folder-add" onclick="addInputForProductToFolder(${product.id}, this)" title="폴더에 추가">
            <svg xmlns="http://www.w3.org/2000/svg" width="24px" fill="currentColor" class="bi bi-folder-plus" viewBox="0 0 16 16">
                <path d="M.5 3l.04.87a1.99 1.99 0 0 0-.342 1.311l.637 7A2 2 0 0 0 2.826 14H9v-1H2.826a1 1 0 0 1-.995-.91l-.637-7A1 1 0 0 1 2.19 4h11.62a1 1 0 0 1 .996 1.09L14.54 8h1.005l.256-2.819A2 2 0 0 0 13.81 3H9.828a2 2 0 0 1-1.414-.586l-.828-.828A2 2 0 0 0 6.172 1H2.5a2 2 0 0 0-2 2zm5.672-1a1 1 0 0 1 .707.293L7.586 3H2.19c-.24 0-.47.042-.684.12L1.5 2.98a1 1 0 0 1 1-.98h3.672z"/>
                <path d="M13.5 10a.5.5 0 0 1 .5.5V12h1.5a.5.5 0 0 1 0 1H14v1.5a.5.5 0 0 1-1 0V13h-1.5a.5.5 0 0 1 0-1H13v-1.5a.5.5 0 0 1 .5-.5z"/>
            </svg>
        </span>`;

    return `<div class="product-card">
                <div onclick="window.location.href='${product.link}'">
                    <div class="card-header">
                        <img src="${product.image}" alt="">
                    </div>
                    <div class="card-body">
                        <div class="title">${product.title}</div>
                        <div class="lprice"><span>${numberWithCommas(product.lprice)}</span>원</div>
                        <div class="isgood ${product.lprice > product.myprice ? 'none' : ''}">최저가</div>
                    </div>
                </div>
                <div class="product-tags" style="margin-bottom: 20px;">
                    ${folders}${addBtn}
                </div>
            </div>`;
}

function setMyprice() {
    /**
     * 1. id가 myprice 인 input 태그에서 값을 가져옴
     * 2. 만약 값을 입력하지 않았으면 alert를 띄우고 중단
     * 3. PUT /api/product/${targetId} 에 data를 전송
     *    주의) contentType: "application/json",
     *         data: JSON.stringify({myprice: myprice}),
     *         빠뜨리지 말 것!
     * 4. 모달을 종료 $('#container').removeClass('active');
     * 5, 성공적으로 등록되었음을 알리는 alert 발생
     * 6. 창 새로고침 window.location.reload();
     */

        // 1. id가 myprice 인 input 태그에서 값을 가져옴
    let myprice = $('#myprice').val();
    // 2. 만약 값을 입력하지 않았으면 alert를 띄우고 중단
    if (myprice == '') {
        alert('올바른 가격을 입력해주세요');
        return;
    }

    // 3. PUT /api/product/${targetId} 에 data를 전송
    $.ajax({
        type: 'PUT',
        url: `/api/products/${targetId}`,
        contentType: 'application/json',
        data: JSON.stringify({myprice: myprice}),
        success: function (response) {

            // 4. 모달 종료 $('#container').removeClass('active');
            $('#container').removeClass('active');
            // 5. 성공적으로 등록되었음을 알리는 alert 발생
            alert('성공적으로 등록되었습니다.');
            // 6. 창 새로고침 window.location.reload();
            window.location.reload();
        },
        error(error, status, request) {
            logout();
        }
    })
}

function logout() {
    localStorage.removeItem('Authorization');
    window.location.href = host + '/api/user/login-page';
}

function getToken() {
    return localStorage.getItem('Authorization') || '';
}

function loadUserFolders() {
    $.ajax({
        type: 'GET',
        url: `/api/user-folder`
    })
        .done(function (fragment, textStatus, xhr) {
            // ✅ 로그인 HTML이 섞여 들어오면 삽입하지 않고 로그아웃
            const ct = (xhr.getResponseHeader('Content-Type') || '').toLowerCase();
            const isHtml = ct.includes('text/html') || typeof fragment === 'string';
            const looksLikeLogin =
                isHtml &&
                typeof fragment === 'string' &&
                (fragment.includes('/api/user/login')
                    || fragment.toLowerCase().includes('name="username"')
                    || fragment.toLowerCase().includes('login'));
            if (looksLikeLogin) {
                logout();
                return;
            }

            $('#fragment').replaceWith(fragment);
        })
        .fail(function (xhr) {
            // ✅ 인증 실패만 로그아웃, 나머지는 경고만
            if (xhr.status === 401 || xhr.status === 403) {
                logout();
            } else {
                console.warn('user-folder load failed:', xhr.status);
            }
        });
}

// 폴더 버튼 클릭 시: 해당 폴더로 필터 후 목록 갱신
function openFolder(folderId) {
    folderTargetId = (folderId === undefined ? null : folderId);
    // 버튼 활성화 표시(버튼 DOM은 서버 프래그먼트가 제공한다고 가정)
    $("button.product-folder").removeClass("folder-active");
    if (!folderId) {
        $("button#folder-all").addClass('folder-active');
    } else {
        $(`button.product-folder[value='${folderId}']`).addClass('folder-active');
    }
    showProduct(); // folderTargetId 를 읽어 데이터 소스를 결정
}

// 폴더 추가 모달 열기 (#container2 사용)
function openAddFolderPopup() {
    $('#container2').addClass('active');
}

// 폴더 입력창 추가
function addFolderInput() {
    $('#folders-input').append(
        `<input type="text" class="folderToAdd" placeholder="추가할 폴더명">
         <span onclick="closeFolderInput(this)" style="margin-right:5px">
            <svg xmlns="http://www.w3.org/2000/svg" width="30px" fill="red" class="bi bi-x-circle-fill" viewBox="0 0 16 16">
              <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM5.354 4.646a.5.5 0 1 0-.708.708L7.293 8l-2.647 2.646a.5.5 0 0 0 .708.708L8 8.707l2.646 2.647a.5.5 0 0 0 .708-.708L8.707 8l2.647-2.646a.5.5 0 0 0-.708-.708L8 7.293 5.354 4.646z"/>
            </svg>
         </span>`
    );
}

// 폴더 입력창 제거
function closeFolderInput(folder) {
    $(folder).prev().remove();
    $(folder).remove();
}

// 폴더 생성 요청
function addFolder() {
    const folderNames = $('.folderToAdd').toArray().map(input => input.value);
    try {
        folderNames.forEach(name => {
            if (name === '') {
                alert('올바른 폴더명을 입력해주세요');
                throw new Error("stop");
            }
        });
    } catch (e) {
        return;
    }

    $.ajax({
        type: "POST",
        url: `/api/folders`,
        contentType: "application/json",
        data: JSON.stringify({folderNames})
    }).done(function (data, textStatus, xhr) {
        // 성공(200/201) → 모달 닫고 새로고침
        $('#container2').removeClass('active');
        alert('성공적으로 등록되었습니다.');
        window.location.reload();
    }).fail(function (xhr) {
        if (xhr.status === 409) {
            alert("중복된 폴더입니다.");
        } else {
            alert("폴더 생성에 실패했습니다.");
        }
    });
}

// 특정 상품을 폴더에 추가하는 선택 UI 동적 삽입
function addInputForProductToFolder(productId, buttonEl) {
    $.ajax({
        type: 'GET',
        url: `/api/folders`,
        success: function (folders) {
            const options = folders.map(folder => `<option value="${folder.id}">${folder.name}</option>`).join('');
            const form = `
                <span>
                    <form id="folder-select-${productId}" method="post" autocomplete="off" action="/api/products/${productId}/folder">
                        <select name="folderId" form="folder-select-${productId}">
                            ${options}
                        </select>
                        <input type="submit" value="추가" style="padding:5px; font-size:12px; margin-left:5px;">
                    </form>
                </span>`;
            $(form).insertBefore(buttonEl);
            $(buttonEl).remove();
            $(`#folder-select-${productId}`).on('submit', function (e) {
                e.preventDefault();
                $.ajax({
                    type: $(this).prop('method'),
                    url: $(this).prop('action'),
                    data: $(this).serialize(),
                }).done(function (data) {
                    if (data !== '') {
                        alert("중복된 폴더입니다.");
                        return;
                    }
                    alert('성공적으로 등록되었습니다.');
                    window.location.reload();
                }).fail(function () {
                    alert("중복된 폴더입니다.");
                });
            });
        },
        error() {
            logout();
        }
    });
}
