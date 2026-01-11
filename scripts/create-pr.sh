#!/bin/bash

# GitHub PR 생성 자동화 스크립트
# 사용법: ./scripts/create-pr.sh <이슈번호> [PR타입] [base브랜치]

set -e

# ============================================
# 색상 정의
# ============================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================
# 함수 정의
# ============================================

print_error() {
    echo -e "${RED}❌ ERROR: $1${NC}" >&2
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# ============================================
# 인자 파싱
# ============================================

if [ -z "$1" ]; then
    print_error "이슈 번호가 필요합니다."
    echo "사용법: $0 <이슈번호> [PR타입] [base브랜치]"
    echo "예시: $0 21"
    echo "예시: $0 21 FEAT"
    echo "예시: $0 21 FEAT dev"
    exit 1
fi

ISSUE_NUMBER="$1"
PR_TYPE="${2:-}"
BASE_BRANCH="${3:-}"

print_info "이슈 번호: #${ISSUE_NUMBER}"

# ============================================
# Base 브랜치 결정
# ============================================

if [ -z "$BASE_BRANCH" ]; then
    # origin/dev 존재 확인
    if git show-ref --verify --quiet refs/remotes/origin/dev; then
        BASE_BRANCH="dev"
        print_info "Base 브랜치: dev (자동 감지)"
    else
        BASE_BRANCH="main"
        print_info "Base 브랜치: main (기본값)"
    fi
else
    print_info "Base 브랜치: ${BASE_BRANCH} (사용자 지정)"
fi

# ============================================
# Git 정보 수집
# ============================================

# 현재 브랜치
CURRENT_BRANCH=$(git branch --show-current)
if [ -z "$CURRENT_BRANCH" ]; then
    print_error "현재 브랜치를 확인할 수 없습니다."
    exit 1
fi
print_info "현재 브랜치: ${CURRENT_BRANCH}"

# remote fetch
print_info "Remote 정보를 업데이트 중..."
git fetch origin --quiet

# base 브랜치 존재 확인
if ! git show-ref --verify --quiet refs/remotes/origin/${BASE_BRANCH}; then
    print_error "Base 브랜치 origin/${BASE_BRANCH}가 존재하지 않습니다."
    exit 1
fi

# 커밋 메시지 목록 (origin/base..HEAD)
COMMIT_MESSAGES=$(git log origin/${BASE_BRANCH}..HEAD --pretty=format:"%s" 2>/dev/null || echo "")

if [ -z "$COMMIT_MESSAGES" ]; then
    print_warning "origin/${BASE_BRANCH}..HEAD에 커밋이 없습니다."
fi

# 변경 파일 통계
CHANGED_FILES=$(git diff --name-only origin/${BASE_BRANCH}...HEAD 2>/dev/null || echo "")
NUMSTAT=$(git diff --numstat origin/${BASE_BRANCH}...HEAD 2>/dev/null || echo "")

# ============================================
# PR 타입 추론
# ============================================

if [ -z "$PR_TYPE" ]; then
    print_info "PR 타입을 커밋 메시지에서 추론 중..."

    # 첫 번째 커밋의 prefix 확인
    FIRST_COMMIT=$(echo "$COMMIT_MESSAGES" | head -n 1)

    if [[ "$FIRST_COMMIT" =~ ^feat ]]; then
        PR_TYPE="FEAT"
    elif [[ "$FIRST_COMMIT" =~ ^fix ]]; then
        PR_TYPE="FIX"
    elif [[ "$FIRST_COMMIT" =~ ^refactor ]]; then
        PR_TYPE="REFACTOR"
    elif [[ "$FIRST_COMMIT" =~ ^docs ]]; then
        PR_TYPE="DOCS"
    elif [[ "$FIRST_COMMIT" =~ ^(chore|test) ]]; then
        PR_TYPE="CHORE"
    else
        PR_TYPE="CHORE"
        print_warning "PR 타입을 추론할 수 없어 CHORE로 설정합니다."
    fi

    print_info "추론된 PR 타입: ${PR_TYPE}"
else
    print_info "PR 타입: ${PR_TYPE} (사용자 지정)"
fi

# ============================================
# PR 제목 생성
# ============================================

# SUMMARY 생성: 대표 커밋 제목에서 prefix와 scope 제거
if [ -n "$COMMIT_MESSAGES" ]; then
    # 첫 번째 커밋에서 prefix 제거 (feat:, fix:, feat(scope): 등)
    SUMMARY=$(echo "$COMMIT_MESSAGES" | head -n 1 | sed -E 's/^[a-z]+(\([^)]+\))?:\s*//')

    # 35자 제한
    if [ ${#SUMMARY} -gt 35 ]; then
        SUMMARY="${SUMMARY:0:32}..."
    fi
else
    # 커밋이 없으면 변경 파일 기반으로 요약 생성
    if [ -n "$CHANGED_FILES" ]; then
        # 상위 디렉토리 추출
        TOP_DIR=$(echo "$CHANGED_FILES" | head -n 1 | cut -d'/' -f1-2)
        SUMMARY="${TOP_DIR} 변경"
    else
        SUMMARY="변경 사항"
    fi
fi

PR_TITLE="[#${ISSUE_NUMBER}][${PR_TYPE}] ${SUMMARY}"

print_success "PR 제목: ${PR_TITLE}"

# ============================================
# 체크박스 자동 선택
# ============================================

case "$PR_TYPE" in
    FEAT)
        CHECKBOX_FEAT="- [x] 기능 추가"
        CHECKBOX_FIX="- [ ] 버그 수정"
        CHECKBOX_REFACTOR="- [ ] 코드 리팩토링"
        CHECKBOX_DOCS="- [ ] 문서 수정"
        CHECKBOX_CHORE="- [ ] 기타 (설명)"
        ;;
    FIX)
        CHECKBOX_FEAT="- [ ] 기능 추가"
        CHECKBOX_FIX="- [x] 버그 수정"
        CHECKBOX_REFACTOR="- [ ] 코드 리팩토링"
        CHECKBOX_DOCS="- [ ] 문서 수정"
        CHECKBOX_CHORE="- [ ] 기타 (설명)"
        ;;
    REFACTOR)
        CHECKBOX_FEAT="- [ ] 기능 추가"
        CHECKBOX_FIX="- [ ] 버그 수정"
        CHECKBOX_REFACTOR="- [x] 코드 리팩토링"
        CHECKBOX_DOCS="- [ ] 문서 수정"
        CHECKBOX_CHORE="- [ ] 기타 (설명)"
        ;;
    DOCS)
        CHECKBOX_FEAT="- [ ] 기능 추가"
        CHECKBOX_FIX="- [ ] 버그 수정"
        CHECKBOX_REFACTOR="- [ ] 코드 리팩토링"
        CHECKBOX_DOCS="- [x] 문서 수정"
        CHECKBOX_CHORE="- [ ] 기타 (설명)"
        ;;
    CHORE)
        CHECKBOX_FEAT="- [ ] 기능 추가"
        CHECKBOX_FIX="- [ ] 버그 수정"
        CHECKBOX_REFACTOR="- [ ] 코드 리팩토링"
        CHECKBOX_DOCS="- [ ] 문서 수정"
        CHECKBOX_CHORE="- [x] 기타 (설명)"
        ;;
    *)
        CHECKBOX_FEAT="- [ ] 기능 추가"
        CHECKBOX_FIX="- [ ] 버그 수정"
        CHECKBOX_REFACTOR="- [ ] 코드 리팩토링"
        CHECKBOX_DOCS="- [ ] 문서 수정"
        CHECKBOX_CHORE="- [ ] 기타 (설명)"
        ;;
esac

# ============================================
# 주요 변경 사항 생성
# ============================================

MAIN_CHANGES=""

if [ -n "$NUMSTAT" ]; then
    # 폴더별로 변경 사항 그룹화
    declare -A folder_stats

    while IFS=$'\t' read -r added deleted file; do
        if [ -z "$file" ]; then
            continue
        fi

        # 상위 1-2 레벨 폴더 추출
        if [[ "$file" == */* ]]; then
            folder=$(echo "$file" | cut -d'/' -f1-2)
        else
            folder=$(echo "$file" | cut -d'/' -f1)
        fi

        # 통계 누적
        if [ -z "${folder_stats[$folder]}" ]; then
            folder_stats[$folder]="$added:$deleted:1"
        else
            IFS=':' read -r old_added old_deleted old_count <<< "${folder_stats[$folder]}"
            new_added=$((old_added + added))
            new_deleted=$((old_deleted + deleted))
            new_count=$((old_count + 1))
            folder_stats[$folder]="$new_added:$new_deleted:$new_count"
        fi
    done <<< "$NUMSTAT"

    # 변경 사항 포맷팅
    for folder in "${!folder_stats[@]}"; do
        IFS=':' read -r added deleted count <<< "${folder_stats[$folder]}"

        # 커밋 메시지에서 요약 추출
        summary="요구사항 반영"
        if [ -n "$COMMIT_MESSAGES" ]; then
            # 첫 번째 커밋에서 간단한 요약 추출
            commit_summary=$(echo "$COMMIT_MESSAGES" | head -n 1 | sed -E 's/^[a-z]+(\([^)]+\))?:\s*//')
            if [ ${#commit_summary} -lt 50 ]; then
                summary="$commit_summary"
            fi
        fi

        MAIN_CHANGES="${MAIN_CHANGES}- \`${folder}\`: +${added}/-${deleted} (${count} files) — ${summary}\n"
    done
else
    MAIN_CHANGES="- 변경 사항 없음"
fi

# ============================================
# PR 본문 생성
# ============================================

PR_BODY=$(cat <<EOF
## PR 요약
> 이 PR이 어떤 변경을 하는지 간단히 설명하고, 체크 표시는 괄호 사이에 소문자 'x'를 삽입하세요.

${CHECKBOX_FEAT}
${CHECKBOX_FIX}
${CHECKBOX_REFACTOR}
${CHECKBOX_DOCS}
${CHECKBOX_CHORE}

---

## 이슈 번호
- #${ISSUE_NUMBER}

---

## 주요 변경 사항
> 주요 파일, 로직, 컴포넌트 등을 구체적으로 적어주세요.

$(echo -e "$MAIN_CHANGES")

---

## 참고 사항
> 리뷰어가 알아야 할 추가 정보, 테스트 방법 등을 작성해주세요.

예:
- 테스트 계정 정보
- 관련 API 엔드포인트
- 로컬 테스트 방법

---
EOF
)

# ============================================
# 파일 저장
# ============================================

echo "$PR_TITLE" > PR_TITLE.txt
echo "$PR_BODY" > PR_BODY.md

print_success "PR 제목이 PR_TITLE.txt에 저장되었습니다."
print_success "PR 본문이 PR_BODY.md에 저장되었습니다."

# ============================================
# PR 생성
# ============================================

if command -v gh &> /dev/null; then
    print_info "gh CLI를 사용하여 PR을 생성합니다..."

    # PR 생성
    if gh pr create \
        --base "$BASE_BRANCH" \
        --head "$CURRENT_BRANCH" \
        --title "$PR_TITLE" \
        --body-file PR_BODY.md; then
        print_success "PR이 성공적으로 생성되었습니다!"
    else
        print_error "PR 생성에 실패했습니다."
        exit 1
    fi
else
    print_warning "gh CLI가 설치되어 있지 않습니다."
    print_info "다음 명령어로 수동으로 PR을 생성할 수 있습니다:"
    echo ""
    echo "gh pr create --base $BASE_BRANCH --head $CURRENT_BRANCH --title \"$PR_TITLE\" --body-file PR_BODY.md"
    echo ""
fi
