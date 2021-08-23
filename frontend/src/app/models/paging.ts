import { PageEvent } from "@angular/material/paginator";

/** Pagination info */
export interface Paging {
  /** page number */
  page: number;
  /** page size */
  limit: number;
  /** total number of items */
  total: number;
}

/**
 * Constants related paging
 */
export class PagingConst {
  /** Get default paging limit options */
  public static getPagingLimitOptions(): number[] {
    return [10, 20, 50];
  }
}

/**
 * Controller for pagination, internal properties are non-private, thus can be directly bound with directive
 */
export class PagingController {
  PAGE_LIMIT_OPTIONS: number[] = PagingConst.getPagingLimitOptions();
  paging: Paging = {
    page: 1,
    limit: this.PAGE_LIMIT_OPTIONS[0],
    total: 0,
  };
  pages: number[] = [1];

  /** Update the list of pages that it can select */
  public updatePages(total: number): void {
    this.pages = [];
    this.paging.total = total;
    let maxPage = Math.ceil(total / this.paging.limit);
    for (let i = 1; i <= maxPage; i++) {
      this.pages.push(i);
    }
    if (this.pages.length === 0) {
      this.pages.push(1);
    }
  }

  /** Set page number */
  public setPage(page: number): void {
    this.paging.page = page;
  }

  /** Set Page limit */
  public setPageLimit(limit: number): void {
    this.paging.limit = limit;
  }

  /** Whether it can go to the next page */
  public canGoToNextPage(): boolean {
    return this.paging.page < this.pages[this.pages.length - 1];
  }

  public nextPage(): void {
    ++this.paging.page;
  }

  public prevPage(): void {
    --this.paging.page;
  }

  /** Whether it can go to the prevous page */
  public canGoToPrevPage(): boolean {
    return this.paging.page > 1;
  }

  public resetCurrentPage(): void {
    this.paging.page = 1;
  }

  public handle(e: PageEvent): void {
    console.log(e);
    this.paging.page = e.pageIndex + 1;
    this.paging.limit = e.pageSize;
  }
}
