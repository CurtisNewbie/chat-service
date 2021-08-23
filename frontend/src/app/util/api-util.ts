const BASE_API = "/api";

export function buildApiPath(subPath: string): string {
  return BASE_API + subPath;
}
