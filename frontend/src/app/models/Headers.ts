import { HttpHeaders } from '@angular/common/http';

export const HEADERS = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
  withCredentials: true,
};
