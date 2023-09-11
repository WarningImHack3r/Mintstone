<script lang="ts">
	import { onMount } from "svelte";
	import type { BannedEntry, Player, Server, WhitelistedPlayer } from "$lib/utils/BackendTypes";
	import { DEFAULT_RCON_PORT, api, avatarFromPlayerUUID, disableTypeCheck } from "$lib/utils";
	import Table from "../Table.svelte";
	import {
		CircleIcon,
		SlashIcon,
		UserIcon,
		UserMinusIcon,
		UserPlusIcon
	} from "svelte-feather-icons";

	export let server: Server;

	let onlinePlayers: Player[] = [];
	let whitelistedPlayers: WhitelistedPlayer[] = [];
	let bannedPlayers: BannedEntry[] = [];

	async function getOnlinePlayers() {
		return (
			await api<{ players: Player[] }>(
				`/rcon/playerslist?${new URLSearchParams({
					serverAddress: server.address,
					serverPort: server.rconPort.toString(),
					serverPassword: server.rconPassword
				})}`
			)
		).players;
	}

	async function getWhitelistedPlayers() {
		return (
			await api<{ whitelist: WhitelistedPlayer[] }>(
				`/rcon/whitelist?${new URLSearchParams({
					serverAddress: server.address,
					serverPort: server.rconPort.toString(),
					serverPassword: server.rconPassword
				})}`
			)
		).whitelist;
	}

	async function getBannedPlayers() {
		return (
			await api<{ banlist: BannedEntry[] }>(
				`/rcon/banlist?${new URLSearchParams({
					serverAddress: server.address,
					serverPort: server.rconPort.toString(),
					serverPassword: server.rconPassword
				})}`
			)
		).banlist;
	}

	onMount(async () => {
		onlinePlayers = await getOnlinePlayers();
		whitelistedPlayers = await getWhitelistedPlayers();
		bannedPlayers = await getBannedPlayers();
	});
</script>

<div class="mx-4 flex flex-wrap justify-between gap-8">
	<!-- Online: Open profile, ban, whitelist, kick -->
	<Table
		title="Online"
		bind:collection={onlinePlayers}
		rowImgSrc={async player => {
			const blob = await avatarFromPlayerUUID(player.uuid);
			return await new Promise((resolve, reject) => {
				const reader = new FileReader();
				reader.onload = () => {
					if (reader.result) {
						const result =
							typeof reader.result === "string"
								? reader.result
								: new TextDecoder().decode(reader.result);
						resolve(result);
					}
					reject("No result");
				};
				reader.onerror = () => reject(reader.error);
				reader.readAsDataURL(blob);
			});
		}}
		rowTitle={player => player.name}
		rowSubtitle={player => player.uuid}
		rowActions={item => [
			/*{
				text: "Open profile",
				icon: UserIcon,
				action: () => {
					// TODO: go to /servers/:serverId/players/:playerId
				}
			},*/
			{
				text: "Ban",
				icon: SlashIcon,
				action: async () => {
					await api(
						`/rcon/ban?${new URLSearchParams({
							serverAddress: server.address,
							serverPort: server.rconPort.toString(),
							serverPassword: server.rconPassword
						})}`,
						{
							method: "POST",
							headers: {
								"Content-Type": "application/json"
							},
							body: JSON.stringify({
								player: item.name
								// TODO: add modal to enter reason
							})
						}
					);
					onlinePlayers = await getOnlinePlayers();
					bannedPlayers = await getBannedPlayers();
				},
				destructive: true
			},
			{
				text: "Whitelist",
				icon: UserPlusIcon,
				action: async () => {
					await api(
						`/rcon/whitelist/add?${new URLSearchParams({
							serverAddress: server.address,
							serverPort: server.rconPort.toString(),
							serverPassword: server.rconPassword
						})}`,
						{
							method: "POST",
							headers: {
								"Content-Type": "application/json"
							},
							body: JSON.stringify({
								player: item.name
							})
						}
					);
					whitelistedPlayers = await getWhitelistedPlayers();
				},
				destructive: true
			},
			{
				text: "Kick",
				icon: UserMinusIcon,
				action: async () => {
					await api(
						`/rcon/kick?${new URLSearchParams({
							serverAddress: server.address,
							serverPort: server.rconPort.toString(),
							serverPassword: server.rconPassword
						})}`,
						{
							method: "POST",
							headers: {
								"Content-Type": "application/json"
							},
							body: JSON.stringify({
								player: item.name
								// TODO: add modal to enter reason
							})
						}
					);
					onlinePlayers = await getOnlinePlayers();
				},
				destructive: true
			}
		]}
	/>
	<!-- Whitelisted: Open profile, remove, ban -->
	<Table
		title="Whitelisted"
		bind:collection={whitelistedPlayers}
		rowTitle={player => player.name}
		enableAdd
		rowActions={item => [
			/*{
				text: "Open profile",
				icon: UserIcon,
				action: () => {
					// TODO: go to /servers/:serverId/players/:playerId
				}
			},*/
			{
				text: "Remove",
				icon: UserMinusIcon,
				action: async () => {
					await api(
						`/rcon/whitelist/remove?${new URLSearchParams({
							serverAddress: server.address,
							serverPort: server.rconPort.toString(),
							serverPassword: server.rconPassword
						})}`,
						{
							method: "POST",
							headers: {
								"Content-Type": "application/json"
							},
							body: JSON.stringify({
								player: item.name
							})
						}
					);
					whitelistedPlayers = await getWhitelistedPlayers();
				},
				destructive: true
			},
			{
				text: "Ban",
				icon: SlashIcon,
				action: async () => {
					await api(
						`/rcon/ban?${new URLSearchParams({
							serverAddress: server.address,
							serverPort: server.rconPort.toString(),
							serverPassword: server.rconPassword
						})}`,
						{
							method: "POST",
							headers: {
								"Content-Type": "application/json"
							},
							body: JSON.stringify({
								player: item.name
								// TODO: add modal to enter reason
							})
						}
					);
					onlinePlayers = await getOnlinePlayers();
					bannedPlayers = await getBannedPlayers();
				},
				destructive: true
			}
		]}
	/>
	<!-- Banned: Open profile, unban, whitelist -->
	<Table
		title="Banned"
		bind:collection={bannedPlayers}
		rowTitle={player => (player.name ? player.name : player.ip) ?? "?"}
		rowSubtitle={player => `${player.reason} (${player.bannedBy})`}
		enableAdd
		rowActions={item => [
			/*{
				text: "Open profile",
				icon: UserIcon,
				action: () => {
					// TODO: go to /servers/:serverId/players/:playerId
				}
			},*/
			{
				text: "Unban",
				icon: CircleIcon,
				action: async () => {
					await api(
						`/rcon/pardon${!!item.name ? "" : "-ip"}?${new URLSearchParams({
							serverAddress: server.address,
							serverPort: server.rconPort.toString(),
							serverPassword: server.rconPassword
						})}`,
						{
							method: "POST",
							headers: {
								"Content-Type": "application/json"
							},
							body: JSON.stringify(
								!!item.name
									? {
											player: item.name
									  }
									: {
											ip: item.ip
									  }
							)
						}
					);
					bannedPlayers = await getBannedPlayers();
				},
				destructive: true
			},
			{
				text: "Whitelist",
				icon: UserPlusIcon,
				action: async () => {
					if (!item.name) return;
					await api(
						`/rcon/whitelist/add?${new URLSearchParams({
							serverAddress: server.address,
							serverPort: server.rconPort.toString(),
							serverPassword: server.rconPassword
						})}`,
						{
							method: "POST",
							headers: {
								"Content-Type": "application/json"
							},
							body: JSON.stringify({
								player: item.name
							})
						}
					);
					whitelistedPlayers = await getWhitelistedPlayers();
				},
				destructive: true
			}
		]}
	/>
</div>
