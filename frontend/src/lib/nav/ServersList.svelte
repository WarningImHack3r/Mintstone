<script lang="ts">
	import { ListBox, ListBoxItem, drawerStore, popup } from "@skeletonlabs/skeleton";
	import { EditIcon, MoreVerticalIcon, TrashIcon } from "svelte-feather-icons";

	function drawerClose(target: EventTarget | null) {
		// Dirty hack to prevent closing the drawer when clicking on the menu button
		if (target instanceof SVGElement) {
			target = target.parentElement;
		}
		if (target instanceof HTMLButtonElement && target.classList.contains("btn-icon")) {
			return;
		}

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
				<a
					href="/"
					class={index === 0 ? "!variant-soft-primary" : ""}
					on:click={e => drawerClose(e.target)}
				>
					<span class="variant-soft-tertiary badge-icon p-4">S</span>
					<span class="flex-auto">
						<dt class="font-bold">{server.name}</dt>
						<dd class="text-sm opacity-50">{server.ip}</dd>
					</span>
					<button
						type="button"
						class="btn-icon btn-icon-lg"
						use:popup={{
							event: "click",
							target: `serverMenu${index}`
						}}
					>
						<MoreVerticalIcon />
					</button>
					<div class="card z-10 w-48 p-4 shadow-xl" data-popup="serverMenu{index}">
						<div class="bg-surface-100-800-token arrow" />
						<ListBox>
							<ListBoxItem group="" name="server-{index}-menu" value="edit">
								<svelte:fragment slot="lead">
									<EditIcon />
								</svelte:fragment>
								Edit
							</ListBoxItem>
							<hr />
							<ListBoxItem
								group=""
								name="server-{index}-menu"
								value="delete"
								class="text-error-500"
							>
								<svelte:fragment slot="lead">
									<TrashIcon />
								</svelte:fragment>
								Delete
							</ListBoxItem>
						</ListBox>
					</div>
				</a>
			</li>
		{/each}
	</ul>
</nav>
