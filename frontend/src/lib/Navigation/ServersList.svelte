<script>
	import { drawerStore } from "@skeletonlabs/skeleton";

    function drawerClose() {
        drawerStore.close();
    }
</script>

<nav class="list-nav p-4">
    <ul>
        <li><a href="/" on:click={drawerClose}>Server 1</a></li>
        <li><a href="/" on:click={drawerClose}>Server 2</a></li>
    </ul>
</nav>
