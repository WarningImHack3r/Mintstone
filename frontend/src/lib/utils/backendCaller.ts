export async function api<T>(route: string, init?: Parameters<typeof fetch>[1]) {
    const response = await fetch(`http://localhost:8080${route}`, init);
    if (!response.ok) {
        throw new Error(response.statusText);
    }
    return response.json() as Promise<T>;
}
