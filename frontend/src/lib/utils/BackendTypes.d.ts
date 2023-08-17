export type UpdateCheck = {
    status: "success";
    updateAvailable: false;
} | {
    status: "success";
    updateAvailable: true;
    latestVersion: string;
    downloadUrl: string;
    changelog: NewVersionChange[];
} | {
    status: "error";
    message: string;
};

type NewVersionChange = {
    version: string;
    changes: string[];
};

export type Version = {
    status: "success";
    platform: Platform;
    version: string;
};

type Platform = "Paper" | "Spigot" | "Bukkit" | "Vanilla" | "Unknown";

export type Server = {
    id: string;
    name: string;
    icon?: string;
    address: string;
    password: string;
    rconPort?: number;
    rconPassword?: string;
    gameVersion: string;
};
