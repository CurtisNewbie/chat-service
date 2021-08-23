export function hasText(t: string): boolean {
  if (t == null) return false;
  if (t.trim().length === 0) return false;
  return true;
}
