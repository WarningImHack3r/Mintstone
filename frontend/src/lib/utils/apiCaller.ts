export async function api<T>(route: string, init?: Parameters<typeof fetch>[1]) {
    const response = await fetch(`http://localhost:8080${route}`, init);
    if (!response.ok) {
        throw new Error(response.statusText);
    }
    return response.json() as Promise<T>;
}

export async function getMinecraftVersions() {
    const response = await fetch(`https://api.curseforge.com/v1/minecraft/version`);
    if (!response.ok) {
        throw new Error(response.statusText);
    }
    return (await (response.json() as Promise<{"data": MinecraftGameVersion[]}>)).data;
}

type MinecraftGameVersion = {
    id: number;
    gameVersionId: number;
    versionString: string;
    jarDownloadUrl: string;
    jsonDownloadUrl: string;
    approved: boolean;
    dateModified: string;
    gameVersionTypeId: number;
    gameVersionStatus: GameVersionStatus;
    gameVersionTypeStatus: GameVersionTypeStatus;
};

enum GameVersionStatus {
    Approved = 1,
    Deleted = 2,
    New = 3,
};

enum GameVersionTypeStatus {
    Normal = 1,
    Deleted = 2,
};
