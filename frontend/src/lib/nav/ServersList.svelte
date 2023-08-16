<script lang="ts">
	import { drawerStore } from "@skeletonlabs/skeleton";
	import { MoreVerticalIcon } from "svelte-feather-icons";

	function drawerClose() {
		drawerStore.close();
	}

	const servers: { name: string; ip: string }[] = [
		{
			name: "Server 1",
			ip: "192.168.1.19"
		},
		{
			name: "Server 2",
			ip: "192.168.1.20"
		}
	];
</script>

<nav class="list-nav p-4">
	<h3 class="h3 py-4 pl-4">Servers</h3>
	<ul>
		{#each servers as server, index}
			<li>
				<a href="/" class={index === 0 ? "!bg-primary-500/50" : ""} on:click={drawerClose}>
					<span class="variant-soft-tertiary badge-icon p-4">H</span>
					<span class="flex-auto">
						<dt class="font-bold">{server.name}</dt>
						<dd class="text-sm opacity-50">{server.ip}</dd>
					</span>
					<button type="button" class="btn-icon btn-icon-lg">
						<MoreVerticalIcon />
					</button>
				</a>
			</li>
		{/each}
	</ul>
</nav>
